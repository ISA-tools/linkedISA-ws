package org.isatools.isa2owl.mapping;

import org.apache.log4j.Logger;
import org.isatools.graph.model.impl.MaterialNode;
import org.isatools.syntax.ExtendedISASyntax;
import org.isatools.util.Pair;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates ISA to OWL mapping information. All data validation is done in this class.
 * 
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 *
 */
public class ISASyntax2OWLMapping {

    private static final Logger log = Logger.getLogger(ISASyntax2OWLMapping.class);

    public static String SEPARATOR_REGEXPR = "\\|";
    public static String CHAIN_PROPERTY = "chain";
	
	Map<String,IRI> sourceOntoIRIs = null;
	Map<String, IRI> typeMappings = null;

    //property mappings
	Map<String, List<Pair<IRI, String>>> propertyMappings = null;
    Map<String,List<Pair<IRI, String>>> contactPropertyMappings = null;
    Map<String,List<Pair<IRI, String>>> protocolPropertyMappings = null;
    Map<String,List<Pair<IRI, String>>> protocolREFPropertyMappings = null;
    Map<String,List<Pair<IRI, String>>> materialNodePropertyMappings = null;

	Map<String, String> patternMappings = null;
	

	public ISASyntax2OWLMapping(){
		init();
	}
	
	private void init(){
		sourceOntoIRIs = new HashMap<String,IRI>();
		typeMappings = new HashMap<String, IRI>();
		propertyMappings = new HashMap<String, List<Pair<IRI,String>>>();
        contactPropertyMappings = new HashMap<String, List<Pair<IRI,String>>>();
        protocolPropertyMappings = new HashMap<String, List<Pair<IRI,String>>>();
        protocolREFPropertyMappings = new HashMap<String, List<Pair<IRI,String>>>();
        materialNodePropertyMappings = new HashMap<String, List<Pair<IRI,String>>>();
		
	}

	/**
	 * 
	 * @param iri ontology IRI 
	 */
	public void addOntology(String name,String iri){
		if (!iri.equals(""))
			sourceOntoIRIs.put(name,IRI.create(iri));
	}

	/**
	 * 
	 * @return list of source ontologies
	 */
	public Map<String,IRI> getSourceOntoIRIs(){
		return sourceOntoIRIs;
	}
	
	public IRI getOntoIRI(String ontoID){
		return sourceOntoIRIs.get(ontoID);
	}

    public IRI getTypeMapping(String label){
        return typeMappings.get(label);
    }

	public void addTypeMapping(String label, String type){
		typeMappings.put(label, IRI.create(type));
	}

    public Map<String,List<Pair<IRI, String>>> getPropertyMappings(){
        return propertyMappings;
    }

    public IRI getPropertyIRI(String subject, String object){
        Map<String,List<Pair<IRI, String>>> map = getPropertyMappings();
        List<Pair<IRI, String>> list = map.get(subject);
        for(Pair<IRI, String> pair: list){
            if (pair.getSecond().equals(object))
                return pair.getFirst();
        }
        return null;
    }


    public IRI getPropertyIRISubjectRegexObjectRegex(String regexSubject, String regexObject){
        //System.out.println("getPropertyIRISubjectRegexObjectRegex="+regexSubject+","+regexObject);
        ArrayList<String> candidates = new ArrayList<String>();

        Pattern pSubject = Pattern.compile(regexSubject);
        Map<String,List<Pair<IRI, String>>> map = getPropertyMappings();

        Set<String> keys = map.keySet();
        Iterator<String> ite = keys.iterator();

        while (ite.hasNext()) {
            String candidate = ite.next();
            Matcher m = pSubject.matcher(candidate);
            //System.out.println("Attempting to match: " + candidate + " to "  + regex);
            if (m.matches()) {
                //  System.out.println("it matches");
                candidates.add(candidate);
            }
        }

        for(String candidate: candidates){

            System.out.println("candidate="+candidate);

            List<Pair<IRI, String>> list = map.get(candidate);
            for(Pair<IRI, String> pair: list){

                //System.out.println("Pair=("+pair.getFirst()+","+pair.getSecond()+")");
                Pattern pObject = Pattern.compile(regexObject);
                Matcher m = pObject.matcher(pair.getSecond());
                if (m.matches()){
                    //System.out.println("it matches... return "+pair.getFirst());
                    return pair.getFirst();
                }
            }


        }
        return null;
    }


    /**
     *
     * @param regex
     * @param object
     * @return
     */
    public IRI getPropertyIRISubjectRegexObject(String regex, String object){

        ArrayList<String> candidates = new ArrayList<String>();

        Pattern p = Pattern.compile(regex);
        Map<String,List<Pair<IRI, String>>> map = getPropertyMappings();

        Set<String> keys = map.keySet();
        Iterator<String> ite = keys.iterator();

        while (ite.hasNext()) {
            String candidate = ite.next();
            Matcher m = p.matcher(candidate);
            //System.out.println("Attempting to match: " + candidate + " to "  + regex);
            if (m.matches()) {
              //  System.out.println("it matches");
                candidates.add(candidate);
            }
        }

        for(String candidate: candidates){
            return getPropertyIRI(candidate, object);
        }
        return null;
    }

    public List<Pair<IRI, String>> getPropertyMappings(String subject){
        return propertyMappings.get(subject);
    }

    public Map<String,List<Pair<IRI, String>>> getContactMappings(){
        return contactPropertyMappings;
    }

    public Map<String,List<Pair<IRI, String>>> getProtocolMappings(){
        return protocolPropertyMappings;
    }

    public Map<String,List<Pair<IRI, String>>> getProtocolREFMappings(){
        return protocolREFPropertyMappings;
    }

    public Map<String,List<Pair<IRI, String>>> getMaterialNodePropertyMappings(){
        return materialNodePropertyMappings;
    }
	
	public void addPropertyMapping(String subject, String predicate, String object){
		List<Pair<IRI,String>> predobjs = propertyMappings.get(subject);
		if (predobjs==null)
			predobjs = new ArrayList<Pair<IRI,String>>();
		if (!predicate.equals("") && !object.equals("")){
			predobjs.add(new Pair<IRI,String>(IRI.create(predicate), object));
		}
		propertyMappings.put(subject, predobjs);

        if (subject.startsWith(ExtendedISASyntax.STUDY_PERSON)){
            contactPropertyMappings.put(subject, predobjs);
        }

        if (subject.startsWith(ExtendedISASyntax.STUDY_PROTOCOL)){
            protocolPropertyMappings.put(subject, predobjs);
        }

        if (subject.startsWith(ExtendedISASyntax.STUDY_PROTOCOL_REF.toString()) || subject.startsWith(ExtendedISASyntax.ASSAY_PROTOCOL_REF.toString())){
            protocolREFPropertyMappings.put(subject, predobjs);
        }

        if (subject.matches(MaterialNode.REGEXP)){
            materialNodePropertyMappings.put(subject, predobjs);
        }

	}


    @Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("MAPPING OBJECT(");
		builder.append("ONTOLOGIES=");
        builder.append(this.mapToString(sourceOntoIRIs));
        builder.append("\nTYPE MAPPINGS=\n");
        builder.append(this.mapToString(typeMappings));
		builder.append("\nPROPERTY MAPPINGS=\n");
		builder.append(this.mapToString(propertyMappings));
        builder.append("\nCONTACT PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(contactPropertyMappings));
        builder.append("\nPROTOCOL PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(protocolPropertyMappings));
        builder.append("\nPROTOCOL REF PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(protocolREFPropertyMappings));
        builder.append("\nMATERIAL NODE PROPERTY MAPPINGS=\n");
        builder.append(this.mapToString(materialNodePropertyMappings));
		builder.append("\nPATTERNS");
		
		return builder.toString();
	}
	
	private <A,B> String mapToString(Map<A, B> map){
		if (map==null)
			return "";
		StringBuilder builder = new StringBuilder();
		for(A key: map.keySet()){
			builder.append(key+ "," + map.get(key)+"\n");
		}
		return builder.toString();
	}


	
}
