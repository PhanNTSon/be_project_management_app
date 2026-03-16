package pma.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestCreateProjectDto {

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 255, message = "Project name cannot exceed 255 characters")
    private String projectName;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}
