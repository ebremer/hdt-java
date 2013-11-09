package org.rdfhdt.hdt.dictionary;
/**
 * File: $HeadURL$
 * Revision: $Rev$
 * Last modified: $Date$
 * Last modified by: $Author$
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Contacting the authors:
 *   Mario Arias:               mario.arias@deri.org
 *   Javier D. Fernandez:       jfergar@infor.uva.es
 *   Miguel A. Martinez-Prieto: migumar2@infor.uva.es
 *   Alejandro Andres:          fuzzy.alej@gmail.com
 */


import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.header.Header;


/**
 * Interface that specifies the basic methods for any Dictionary implementation
 * 
 * @author mario.arias, Eugen
 * 
 */
public interface Dictionary {


	/**
	 * Returns the string for a given id
	 * 
	 * @param id
	 *            The id to convert to string
	 * @param position
	 *            TriplePosition of the id in the dictionary
	 * @return String
	 */
	public CharSequence idToString(int id, TripleComponentRole position);
	
	/**
	 * Returns the id for a given string
	 * 
	 * @param str
	 *            The string to convert to id
	 * @param position
	 *            TriplePosition of the string in the dictionary
	 * @return int
	 */
	public int stringToId(CharSequence str, TripleComponentRole position);

	/**
	 * Returns the number of elements in the dictionary
	 */
	public long getNumberOfElements();

	/**
	 * Return the combined size of the sections of the dictionary (in bytes)
	 */
	public long size();
	
	/**
	 * Returns the number of subjects in the dictionary.
	 */
	public long getNsubjects();
	
	/**
	 * Returns the number of predicates in the dictionary.
	 */
	public long getNpredicates();
	
	/**
	 * Returns the number of objects in the dictionary.
	 */
	public long getNobjects();
	
	/**
	 * Returns the number of subjects/objects in the dictionary.
	 */
	public long getNshared();
		
	public DictionarySection getSubjects();
	
	public DictionarySection getPredicates();
	
	public DictionarySection getObjects();
	
	public DictionarySection getShared();
	
	/**
	 * Fills the header with information from the dictionary
	 */
	public void populateHeader(Header header, String rootNode);
	
	/**
	 * Returns the type of the dictionary (the way it is written onto file/held in memory)
	 * @return
	 */
	public String getType();
}