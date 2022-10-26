package org.rdfhdt.hdt.util;

import org.rdfhdt.hdt.exceptions.NotImplementedException;
import org.rdfhdt.hdt.util.string.CharSequenceComparator;
import org.rdfhdt.hdt.util.string.CompactString;
import org.rdfhdt.hdt.util.string.DelayedString;
import org.rdfhdt.hdt.util.string.ReplazableString;

import java.util.ConcurrentModificationException;

public class LiteralsUtils {
	public static final String NO_DATATYPE_STR = "NO_DATATYPE";
	static final String LITERAL_LANG_TYPE_STR = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString>";
	/**
	 * no datatype type
	 */
	public static final CharSequence NO_DATATYPE = new CompactString(NO_DATATYPE_STR);
	public static final CharSequence LITERAL_LANG_TYPE = new CompactString(LITERAL_LANG_TYPE_STR);

	/**
	 * test if the node is a literal and contains a language
	 *
	 * @param str the node
	 * @return true if the node is a literal and contains a language, false otherwise
	 * @throws java.util.ConcurrentModificationException if the node is updated while reading
	 */
	public static boolean containsLanguage(CharSequence str) {
		if (str.length() == 0 || str.charAt(0) != '"') {
			return false; // not a literal
		}

		for (int i = str.length() - 1; i >= 0; i--) {
			char c = str.charAt(i);

			// https://www.w3.org/TR/n-triples/#n-triples-grammar
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '-') {
				// lang tag, ignore
				continue;
			}

			if (c == '"') {
				// end the literal, no lang tag
				return false;
			}

			// start of the lang tag
			return c == '@';
		}
		throw new ConcurrentModificationException("Update of the char sequence while reading!");
	}

	/**
	 * get the index of the last ^^ of the literal type
	 *
	 * @param str node
	 * @return index of the start of the uri type
	 * @throws java.util.ConcurrentModificationException if the node is updated while reading
	 */
	private static int getTypeIndex(CharSequence str) {
		if (str.length() == 0 || str.charAt(0) != '"' || str.charAt(str.length() - 1) != '>') {
			return -1; // not a literal
		}
		int i = str.length() - 1;

		// find end of the type
		while (i > 0) {
			if (str.charAt(i) == '<' && str.charAt(i - 1) != '\\') {
				break;
			}
			i--;
		}

		char c = str.charAt(i - 1);

		// https://www.w3.org/TR/n-triples/#n-triples-grammar
		if (c == '"' || c == '@') {
			return -1; // no type, syntax error????
		}

		if (c == '^') {
			return i;
		}

		throw new ConcurrentModificationException("Update of the char sequence while reading!");
	}

	/**
	 * test if the node is a literal and contains a language
	 *
	 * @param str the node
	 * @return true if the node is a literal and contains a language, false otherwise
	 * @throws java.util.ConcurrentModificationException if the node is updated while reading
	 */
	public static CharSequence getType(CharSequence str) {
		if (containsLanguage(str)) {
			return LITERAL_LANG_TYPE;
		}

		int index = getTypeIndex(str);

		if (index != -1 && index < str.length()) {
			return str.subSequence(index, str.length());
		} else {
			return NO_DATATYPE;
		}
	}

	/**
	 * remove the node type if the node is a typed literal, this method return the char sequence or a subSequence of this
	 * char sequence
	 *
	 * @param str the node
	 * @return node or the typed literal
	 * @throws java.util.ConcurrentModificationException if the node is updated while reading
	 */
	public static CharSequence removeType(CharSequence str) {
		int index = getTypeIndex(str);

		if (index != -1 && index < str.length()) {
			return str.subSequence(0, index - 2);
		} else {
			return str;
		}
	}

	static boolean isLangType(CharSequence s, int start) {
		if (start + LITERAL_LANG_TYPE_STR.length() > s.length()) {
			return false;
		}
		// we can use the string version because the langString IRI is in ASCII
		for (int i = 0; i < LITERAL_LANG_TYPE_STR.length(); i++) {
			if (s.charAt(i + start) != LITERAL_LANG_TYPE_STR.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * place the type before the literal
	 *
	 * <p>example: {@literal "aa"^^<http://type>} -> {@literal ^^<http://type>"aa"}</p>
	 * <p>example: "aa" -> "aa"</p>
	 * <p>example: "aa"@fr -> {@literal ^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString>"aa"@fr}</p>
	 *
	 * @param str the literal
	 * @return prefixed literal
	 */
	public static CharSequence litToPref(CharSequence str) {
		// language literal
		if (containsLanguage(str)) {
			ReplazableString prefixedValue = new ReplazableString(2 + LITERAL_LANG_TYPE.length() + str.length());
			prefixedValue.append(new byte[]{'^', '^'}, 0, 2);
			prefixedValue.append(((CompactString) LITERAL_LANG_TYPE).getData());
			prefixedValue.appendNoCompact(str);
			return prefixedValue;
		}

		int index = getTypeIndex(str);

		// typed literal
		if (index != -1 && index < str.length()) {
			// add the literal value
			ReplazableString prefixedValue = new ReplazableString(str.length());
			prefixedValue.append(new byte[]{'^', '^'}, 0, 2);
			prefixedValue.appendNoCompact(str, index, str.length() - index);
			prefixedValue.appendNoCompact(str, 0, index - 2);
			return prefixedValue;
		}

		return str;
	}

	/**
	 * remove the type of a prefixed literal
	 *
	 * @param str the prefixed literal
	 * @return literal
	 * @see #removeType(CharSequence)
	 */
	public static CharSequence removePrefType(CharSequence str) {
		if (str.length() < 4 || !(str.charAt(0) == '^' && str.charAt(1) == '^')) {
			// prefixed type
			return str;
		}

		assert str.charAt(2) == '<' : "non typed literal prefix";

		int index = 3;

		while (index < str.length()) {
			char c = str.charAt(index);
			if (c == '>') {
				break;
			}
			index++;
		}
		assert index < str.length() - 1 && str.charAt(index + 1) == '"' : "badly typed literal prefix";

		return str.subSequence(index + 1, str.length());
	}

	/**
	 * replace the literal before the type
	 *
	 * <p>example: {@literal ^^<http://type>"aa"} -> {@literal "aa"^^<http://type>}</p>
	 * <p>example: "aa" -> "aa"</p>
	 * <p>example: {@literal ^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#langString>"aa"@fr} -> "aa"@fr</p>
	 *
	 * @param str the prefixed literal
	 * @return literal
	 */
	public static CharSequence prefToLit(CharSequence str) {
		if (str.length() < 4 || !(str.charAt(0) == '^' && str.charAt(1) == '^')) {
			return str;
		}

		assert str.charAt(2) == '<' : "non typed literal prefix";

		int index = 3;

		if (isLangType(str, 2)) {
			// lang type, return without the type
			return str.subSequence(LITERAL_LANG_TYPE.length() + 2, str.length());
		}

		while (index < str.length()) {
			char c = str.charAt(index);
			if (c == '>') {
				break;
			}
			index++;
		}
		assert index < str.length() - 1 && str.charAt(index + 1) == '"' : "badly typed literal prefix";

		ReplazableString bld = new ReplazableString(str.length());
		bld.appendNoCompact(str, index + 1, str.length() - index - 1);
		bld.appendNoCompact(str, 0, index + 1);
		return bld;
	}

	/**
	 * add {@literal '<'} and {@literal '>'} to a CharSequence, will have the same behaviors as a byte string
	 *
	 * @param s1 string
	 * @return embed version of s1
	 */
	public static CharSequence embed(CharSequence s1) {
		s1 = DelayedString.unwrap(s1);
		if (s1 == null || s1.length() == 0) {
			return EmbeddedURI.EMPTY;
		}
		if (s1.charAt(0) == '<' && s1.charAt(s1.length() - 1) == '>') {
			return s1;
		}
		return new EmbeddedURI(s1);
	}

	private static class EmbeddedURI implements CharSequence {
		private static final CharSequence EMPTY = new CompactString("<>");
		private int hash;
		private final CharSequence parent;

		public EmbeddedURI(CharSequence parent) {
			this.parent = parent;
		}

		@Override
		public int length() {
			return parent.length() + 2;
		}

		@Override
		public char charAt(int index) {
			if (index == 0) {
				return '<';
			}
			if (index == parent.length() + 1) {
				return '>';
			}
			return parent.charAt(index - 1);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start == 0 && end == length()) {
				return this;
			}
			if (start == 0 || end == length()) {
				return new CompactString(this.toString().subSequence(start, end));
			}

			return parent.subSequence(start - 1, end - 1);
		}

		@Override
		public String toString() {
			return "<" + parent + ">";
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}
			if (this == o) {
				return true;
			}
			if (!(o instanceof CharSequence)) {
				return false;
			}

			return CharSequenceComparator.getInstance().compare(this, (CharSequence) o) == 0;
		}

		@Override
		public int hashCode() {
			// FNV Hash function: http://isthe.com/chongo/tech/comp/fnv/
			if (hash == 0) {
				hash = (int) 2166136261L;
				int i = length();

				while (i-- != 0) {
					hash = (hash * 16777619) ^ charAt(i);
				}
			}
			return hash;
		}
	}

	/**
	 * test if a sequence is a No datatype string
	 *
	 * @param seq sequence
	 * @return true if seq == "NO_DATATYPE"
	 */
	public static boolean isNoDatatype(CharSequence seq) {
		if (seq.length() != NO_DATATYPE.length()) {
			return false;
		}
		for (int i = 0; i < NO_DATATYPE.length(); i++) {
			if (NO_DATATYPE.charAt(i) != seq.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
