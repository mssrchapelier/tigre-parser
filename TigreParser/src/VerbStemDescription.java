import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class VerbStemDescription {
	private static final Pattern stemDescriptionPattern = Pattern.compile("^\\$radicals\\:(?<rad>[345]),type\\:(?<type>[ABCD]),prefix\\:(?<prefix>0|A|T|AT|ATTA|AN|AS|ATTAN|ATTAS|ASTA)$");
	
	private final NumRadicals numRadicals;
	private final VerbType verbType;
	private final VerbPreformative derivPrefix;

	VerbStemDescription (NumRadicals numRadicals, VerbType verbType, VerbPreformative derivPrefix) {
		this.numRadicals = numRadicals;
		this.verbType = verbType;
		this.derivPrefix = derivPrefix;
	}

	VerbStemDescription (VerbStemDescription description) {
		this.numRadicals = description.numRadicals;
		this.verbType = description.verbType;
		this.derivPrefix = description.derivPrefix;
	}

	static VerbStemDescription parse (String line) throws ConfigParseException {
		try {
			Matcher matcher = stemDescriptionPattern.matcher(line);
			matcher.find();
			NumRadicals numRadicals = NumRadicals.parseNumRadicals(matcher.group("rad"));
			VerbType verbType = VerbType.parseVerbType(matcher.group("type"));
			VerbPreformative derivPrefix = VerbPreformative.parseVerbPreformative(matcher.group("prefix"));
			return new VerbStemDescription (numRadicals, verbType, derivPrefix);
		} catch (IllegalStateException | ConfigParseException e) {
			String message = String.format("Failed to parse paradigm description: %s", line);
			throw new ConfigParseException(message, e);
		}
	}

	@Override
	public String toString () {
		return String.format("%s-radical, verb type %s, derivational prefix %s",
					this.numRadicals.toString(),
					this.verbType.toString(),
					this.derivPrefix.toString());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(numRadicals)
				.append(verbType)
				.append(derivPrefix)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VerbStemDescription)) { return false; }
		if (obj == this) { return true; }

		VerbStemDescription rhs = (VerbStemDescription) obj;
		return new EqualsBuilder().append(numRadicals, rhs.numRadicals)
					.append(verbType, rhs.verbType)
					.append(derivPrefix, rhs.derivPrefix)
					.isEquals();
	}
}
