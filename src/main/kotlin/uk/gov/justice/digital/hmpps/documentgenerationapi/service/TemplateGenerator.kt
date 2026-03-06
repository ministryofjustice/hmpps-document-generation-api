package uk.gov.justice.digital.hmpps.documentgenerationapi.service

import org.apache.poi.common.usermodel.PictureType
import org.apache.poi.util.Units
import org.apache.poi.xwpf.usermodel.XWPFDocument
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
import java.util.UUID

@Service
class TemplateGenerator(
  private val templateRepository: DocumentTemplateRepository,
  private val dmc: DocumentManagementClient,
  private val download: DocumentDownloader,
  private val dgrRepository: DocumentGenerationRequestRepository,
) {
  fun fromTemplate(
    id: UUID,
    request: GenerateFromTemplate,
    image: MultipartFile?,
  ): ResponseEntity<ByteArray> {
    val template = templateRepository.getDocumentTemplate(id)
    dgrRepository.save(DocumentGenerationRequest(template, request))
    val input = dmc.downloadTemplate(template.externalReference)
    return download.docx(docx(input, request, image?.bytes), request.filename)
  }

  private fun docx(
    file: ByteArray,
    request: GenerateFromTemplate,
    image: ByteArray?,
  ): ByteArray = XWPFDocument(file.inputStream()).use { doc ->
    doc.paragraphs.forEach { para ->
      para.ctp.bookmarkStartList.forEach { bm ->
        val run = para.createRun().apply {
          if (bm.name == PERSON_IMAGE) {
            image?.inputStream()?.use {
              addPicture(it, PictureType.JPEG, IMAGE_FILENAME, IMAGE_WIDTH, IMAGE_HEIGHT)
            }
          } else {
            request.variables[bm.name]?.also { setText(it.toString()) }
          }
        }
        para.ctp.domNode.insertBefore(run.ctr.domNode, bm.domNode)
      }
    }
    doc.`package`.replaceContentType(DOTX_CONTENT_TYPE, DOCX_CONTENT_TYPE)
    ByteArrayOutputStream().apply { doc.write(this) }.toByteArray()
  }

  companion object {
    private const val DOTX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml"
    private const val DOCX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"
    private const val PERSON_IMAGE = "PERSON__IMAGE"
    private const val IMAGE_FILENAME = "person-image"
    private const val IMAGE_HEIGHT = Units.EMU_PER_CENTIMETER * 3
    private const val IMAGE_WIDTH = Units.EMU_PER_CENTIMETER * 3
  }
}
