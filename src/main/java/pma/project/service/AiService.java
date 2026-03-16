package pma.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pma.common.exception.ApiException;
import pma.project.dto.change.UsecasePayloadDto;
import pma.project.dto.request.AiContextPromptDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String generateContextDiagramMermaid(AiContextPromptDto request) {
        String visionScopesText = request.getVisionScopes() != null && !request.getVisionScopes().isEmpty() 
            ? String.join("; ", request.getVisionScopes()) 
            : "N/A";
        String actorsText = request.getActorList() != null && !request.getActorList().isEmpty()
            ? String.join(", ", request.getActorList())
            : "N/A";

        String prompt = String.format("""
            You are an expert software architect.
            Generate a valid Mermaid.js flowchart (graph TD or LR) representing a high-level System Context Diagram for the following project.
            Only return the plain Mermaid code block without any surrounding markdown formatting like `\u200B`\u200B`mermaid or `\u200B`\u200B`.
            
            Project Name: %s
            Description: %s
            Vision/Scope: %s
            Main Actors/Users: %s
            
            The diagram should have one central node representing the System ("%s"), and surrounding nodes representing the actors/external systems interacting with it.
            Keep it simple, readable, and use standard Mermaid syntax.
            """, 
            request.getProjectName(), 
            request.getDescription() != null ? request.getDescription() : "N/A", 
            visionScopesText, 
            actorsText,
            request.getProjectName()
        );

        return callGemini(prompt);
    }

    public String generateUsecaseDiagramMermaid(UsecasePayloadDto request) {
        String normalFlowsText = request.getNormalFlows() != null && !request.getNormalFlows().isEmpty()
            ? String.join("\n", request.getNormalFlows().stream().map(f -> "- " + f).toList())
            : "N/A";
        String alterFlowsText = request.getAlterFlows() != null && !request.getAlterFlows().isEmpty()
            ? String.join("\n", request.getAlterFlows().stream().map(f -> "- " + f).toList())
            : "None";

        String prompt = String.format("""
            You are an expert software architect.
            Generate a valid Mermaid.js flowchart (graph TD) representing the execution flow of the following specific Use Case.
            Only return the plain Mermaid code block without any surrounding markdown formatting like `\u200B`\u200B`mermaid or `\u200B`\u200B`.
            
            Use Case Name: %s
            Primary Actor: %s
            Precondition: %s
            Postcondition: %s
            Normal Flow Steps:
            %s
            Alternative Flow Steps:
            %s
            
            The diagram should use flowchart capabilities to show the start state, the sequence of normal flows, any alternative branching, and the end state.
            Keep the node labels concise. Use standard Mermaid syntax.
            """,
            request.getUsecaseName(),
            request.getActor() != null ? request.getActor() : "User",
            request.getPrecondition() != null ? request.getPrecondition() : "N/A",
            request.getPostcondition() != null ? request.getPostcondition() : "N/A",
            normalFlowsText,
            alterFlowsText
        );

        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty() || geminiApiKey.contains("{GEMINI_API_KEY}")) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API key is not configured.");
        }

        String url = GEMINI_API_URL + geminiApiKey;

        try {
            // Build Gemini Request Payload
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);

            Map<String, Object> parts = new HashMap<>();
            parts.put("parts", List.of(textPart));

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.2);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(parts));
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("Calling Gemini API for Mermaid Code Generation");
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                String mermaidCode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
                return cleanMermaidCode(mermaidCode);
            } else {
                log.error("Gemini API failed with status {}", response.getStatusCode());
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate AI diagram");
            }
        } catch (Exception e) {
            log.error("Exception calling Gemini API: ", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate AI diagram: " + e.getMessage());
        }
    }

    private String cleanMermaidCode(String text) {
        if (text == null) return "";
        String code = text.trim();
        if (code.startsWith("```mermaid")) {
            code = code.substring(10).trim();
        } else if (code.startsWith("```")) {
            code = code.substring(3).trim();
        }
        if (code.endsWith("```")) {
            code = code.substring(0, code.length() - 3).trim();
        }
        return code;
    }
}
