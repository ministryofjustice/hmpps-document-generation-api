package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import jakarta.xml.bind.JAXBElement
import org.docx4j.openpackaging.contenttype.ContentTypes.WORDPROCESSINGML_DOCUMENT
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage
import org.docx4j.wml.CTBookmark
import org.docx4j.wml.CTMarkupRange
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.ObjectFactory
import org.docx4j.wml.R
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentGenerationRequest
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentGenerationRequestRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.DocumentTemplateRepository
import uk.gov.justice.digital.hmpps.documentgenerationapi.domain.getDocumentTemplate
import uk.gov.justice.digital.hmpps.documentgenerationapi.integration.documentmanagement.DocumentManagementClient
import uk.gov.justice.digital.hmpps.documentgenerationapi.model.GenerateFromTemplate
import java.io.ByteArrayOutputStream
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.reflect.full.cast

@Service
class DocumentGenerator(
  private val templateRepository: DocumentTemplateRepository,
  private val dmc: DocumentManagementClient,
  private val download: DocumentDownloader,
  private val dgrRepository: DocumentGenerationRequestRepository,
) {
  fun fromTemplate(
    id: UUID,
    request: GenerateFromTemplate,
    personImage: MultipartFile?,
  ): ResponseEntity<ByteArray> {
    val template = templateRepository.getDocumentTemplate(id)
    dgrRepository.save(DocumentGenerationRequest(template, request))
    val input = dmc.downloadTemplate(template.externalReference)
    return download.docx(docx(input, request, personImage?.bytes), request.filename)
  }

  private fun docx(
    file: ByteArray,
    request: GenerateFromTemplate,
    personImage: ByteArray?,
  ): ByteArray = WordprocessingMLPackage.load(file.inputStream()).let { wp ->
    val extraVariables = listOfNotNull(
      personImage?.let { PERSON_IMAGE to it },
      DATE_NOW to LocalDate.now(),
      DATE_TIME_NOW to LocalDateTime.now(),
    ).toMap()
    wp.replaceBookmarks(request.variables + extraVariables)
    wp.contentTypeManager.addOverrideContentType(URI("/word/document.xml"), WORDPROCESSINGML_DOCUMENT)
    ByteArrayOutputStream().apply { wp.save(this) }.toByteArray()
  }

  private fun WordprocessingMLPackage.replaceBookmarks(data: Map<String, Any>) {
    mainDocumentPart.getAllBookmarks().forEach { bookmark ->
      if (data.containsKey(bookmark.name)) {
        val parent = bookmark.parent as? ContentAccessor ?: return@forEach
        val start = parent.content.indexOf(bookmark)
        val end =
          parent.content.indexOfLast { it is JAXBElement<*> && it.value is CTMarkupRange && (it.value as CTMarkupRange).id == bookmark.id }

        for (i in end - 1 downTo start + 1) {
          parent.content.removeAt(i)
        }

        when (val value = data[bookmark.name]) {
          is ByteArray -> parent.content.add(start + 1, runWithImage(value))
          else -> parent.content.add(start + 1, runWithText(value.toString()))
        }
      }
    }
  }

  private fun Any.getAllBookmarks(): List<CTBookmark> = when (this) {
    is JAXBElement<*> -> value.getAllBookmarks()
    is ContentAccessor -> content.flatMap { it.getAllBookmarks() }
    is CTBookmark -> listOf(CTBookmark::class.cast(this))
    else -> listOf()
  }

  private fun runWithText(text: String): R = ObjectFactory().let { factory ->
    factory.createR().also { run ->
      factory.createText().apply {
        value = text
        space = "preserve"
      }.also { run.content.add(it) }
    }
  }

  private fun WordprocessingMLPackage.runWithImage(image: ByteArray): R = ObjectFactory().let { factory ->
    factory.createR().also { run ->
      factory.createDrawing().apply {
        BinaryPartAbstractImage.createImagePart(this@runWithImage, image)
          .createImageInline(IMAGE_FILENAME, IMAGE_FILENAME, 1, 2, false, IMAGE_MAX_WIDTH)
          .also { anchorOrInline.add(it) }
      }.also { run.content.add(it) }
    }
  }

  companion object {
    private const val DATE_NOW = "dateNow"
    private const val DATE_TIME_NOW = "dateTimeNow"
    private const val PERSON_IMAGE = "perImage"
    private const val IMAGE_FILENAME = "person-image"
    private const val IMAGE_MAX_WIDTH = 1800
  }
}
