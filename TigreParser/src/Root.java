import java.util.ArrayList;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Root {
	ArrayList<ConsDescription> consTemplate;
	
	public Root (ArrayList<ConsDescription> template) {
		this.consTemplate = template;
	}
	
	public Root () {
		this.consTemplate = new ArrayList<>();
	}
	
	public static Root newInstance (Root root) {
		ArrayList<ConsDescription> newConsTemplate = new ArrayList<>();
		for (ConsDescription cd : root.consTemplate) {
			newConsTemplate.add(ConsDescription.newInstance(cd));
		}
		return new Root(newConsTemplate);
	}
	
	public int size () {
		return this.consTemplate.size();
	}
	
	@Override
	public String toString () {
		String output = "";
		for (ConsDescription radical : this.consTemplate) {
			String toAppend = "";
			toAppend += radical.consonant;
			if (radical.isGeminated) { toAppend += "(2)"; }
			if (radical.followedByLongA) { toAppend += "(A)"; }
			output += toAppend;
		}
		return output;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113).append(consTemplate).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
       if (!(obj instanceof Root))
            return false;
        if (obj == this)
            return true;

        Root rhs = (Root) obj;
        return new EqualsBuilder()
            .append(consTemplate, rhs.consTemplate)
            .isEquals();
    }
}
