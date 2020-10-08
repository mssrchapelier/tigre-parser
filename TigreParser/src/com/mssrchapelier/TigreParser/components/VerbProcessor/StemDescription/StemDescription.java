package com.mssrchapelier.TigreParser.components.VerbProcessor.StemDescription;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.mssrchapelier.TigreParser.components.utils.misc.ConfigParseException;

public class StemDescription {
	private static final Pattern stemDescriptionPattern = Pattern.compile("^\\$radicals\\:(?<rad>[345]),type\\:(?<type>[ABCD]),prefix\\:(?<prefix>0|A|T|AT|ATTA|AN|AS|ATTAN|ATTAS|ASTA)$");
	
	private final NumRadicals numRadicals;
	private final RootType rootType;
	private final DerivPrefix derivPrefix;

	public StemDescription (NumRadicals numRadicals, RootType rootType, DerivPrefix derivPrefix) {
		this.numRadicals = numRadicals;
		this.rootType = rootType;
		this.derivPrefix = derivPrefix;
	}

	public StemDescription (StemDescription description) {
		this.numRadicals = description.numRadicals;
		this.rootType = description.rootType;
		this.derivPrefix = description.derivPrefix;
	}

	public static StemDescription parse (String line) throws ConfigParseException {
		try {
			Matcher matcher = stemDescriptionPattern.matcher(line);
			matcher.find();
			NumRadicals numRadicals = NumRadicals.parseNumRadicals(matcher.group("rad"));
			RootType rootType = RootType.parseRootType(matcher.group("type"));
			DerivPrefix derivPrefix = DerivPrefix.parseDerivPrefix(matcher.group("prefix"));
			return new StemDescription (numRadicals, rootType, derivPrefix);
		} catch (IllegalStateException | ConfigParseException e) {
			String message = String.format("Failed to parse paradigm description: %s", line);
			throw new ConfigParseException(message, e);
		}
	}

	@Override
	public String toString () {
		return String.format("%s-radical, verb type %s, derivational prefix %s",
					this.numRadicals.toString(),
					this.rootType.toString(),
					this.derivPrefix.toString());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(109, 113)
				.append(numRadicals)
				.append(rootType)
				.append(derivPrefix)
				.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof StemDescription)) { return false; }
		if (obj == this) { return true; }

		StemDescription rhs = (StemDescription) obj;
		return new EqualsBuilder().append(numRadicals, rhs.numRadicals)
					.append(rootType, rhs.rootType)
					.append(derivPrefix, rhs.derivPrefix)
					.isEquals();
	}
}
