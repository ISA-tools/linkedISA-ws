package org.isatools.graph.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.isatools.graph.model.ISAMaterialAttribute;
import org.isatools.graph.model.ISAMaterialNode;
import org.isatools.graph.model.ISANode;
import org.isatools.isacreator.model.GeneralFieldTypes;
import org.isatools.syntax.ExtendedISASyntax;


/**
 * Created by the ISATeam.
 * User: agbeltran
 * Date: 06/11/2012
 * Time: 16:11
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 * @author <a href="mailto:alejandra.gonzalez.beltran@gmail.com">Alejandra Gonzalez-Beltran</a>
 */
public class MaterialNode extends NodeWithComments implements ISAMaterialNode {

    private List<ISAMaterialAttribute> materialAttributes;

    public MaterialNode(int index, String name) {
        super(index, name);
        materialAttributes = new ArrayList<ISAMaterialAttribute>();
    }

    @Override
    public void addMaterialAttribute(ISAMaterialAttribute attribute) {
        materialAttributes.add(attribute);
    }

    @Override
    public List<ISAMaterialAttribute> getMaterialAttributes() {
        return materialAttributes;
    }

    @Override
    public String getMaterialNodeType(){

        if (getName().equals(GeneralFieldTypes.SAMPLE_NAME.toString()))
            return ExtendedISASyntax.SAMPLE;

        if (getName().equals(GeneralFieldTypes.SOURCE_NAME.toString()))
            return ExtendedISASyntax.SOURCE;

        if (getName().equals(GeneralFieldTypes.EXTRACT_NAME.toString()))
            return ExtendedISASyntax.EXTRACT;

        if (getName().equals(GeneralFieldTypes.LABELED_EXTRACT_NAME.toString()))
            return ExtendedISASyntax.LABELED_EXTRACT;

        return null;

    }

    @Override
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("MaterialNode: "+getName()+"\n");
        for (ISANode mp : getMaterialAttributes()) {
            buffer.append("\t attribute: " + mp.getName()+"\n");
        }
        if (getComments() != null) {
            for (Node commentNode : getComments()) {
                buffer.append("\t comment: " + commentNode.getName()+"\n");
            }
        }
        return buffer.toString();
    }

}