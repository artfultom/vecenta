package test.pack.model.v1.entity1;

import io.github.artfultom.vecenta.matcher.annotations.Model;
import io.github.artfultom.vecenta.matcher.annotations.ModelField;
import java.util.List;

@Model(
        name = "ClientNumberOne.entity1.model2",
        order = {"field1"}
)
public class Model2 {
    @ModelField(
            type = "[int32]"
    )
    private List<Integer> field1;

    public Model2() {
    }

    public List<Integer> getField1() {
        return this.field1;
    }

    public void setField1(List<Integer> field1) {
        this.field1 = field1;
    }
}
