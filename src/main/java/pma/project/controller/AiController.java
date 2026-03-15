package pma.project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pma.project.dto.change.UsecasePayloadDto;
import pma.project.dto.request.AiContextPromptDto;
import pma.project.dto.response.AiResponseDto;
import pma.project.service.AiService;

@RestController
@RequestMapping("/api/projects/{projectId}/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-context")
    public ResponseEntity<AiResponseDto> generateContextDiagram(
            @PathVariable Integer projectId,
            @RequestBody AiContextPromptDto request) {
        String mermaidCode = aiService.generateContextDiagramMermaid(request);
        return ResponseEntity.ok(new AiResponseDto(mermaidCode));
    }

    @PostMapping("/generate-usecase")
    public ResponseEntity<AiResponseDto> generateUsecaseDiagram(
            @PathVariable Integer projectId,
            @RequestBody UsecasePayloadDto request) {
        String mermaidCode = aiService.generateUsecaseDiagramMermaid(request);
        return ResponseEntity.ok(new AiResponseDto(mermaidCode));
    }
}
