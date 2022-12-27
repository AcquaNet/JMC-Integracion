package acqua.util.Transform;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(value = {"x","y","z"})
public class TestPojo {
	
	public String x,y,z ;
}
