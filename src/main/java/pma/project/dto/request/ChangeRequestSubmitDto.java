package pma.project.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestSubmitDto {
    private String title;
    private String description;
    private List<ChangeItemSubmitDto> items;
}
