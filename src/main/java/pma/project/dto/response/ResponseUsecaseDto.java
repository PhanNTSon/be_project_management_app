package pma.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUsecaseDto {
    private Integer usecaseId;
    private String usecaseName;
    private String precondition;
    private String postcondition;
    private String exceptions;
    private String priority;
}
