/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.autosparql.tbsl.algorithm.paraphrase;

import java.util.ArrayList;
import java.util.List;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

/**
 *
 * @author ngonga
 */
public class MicrosoftParaphraser implements Paraphraser {

    public List<Language> languages = new ArrayList<Language>();

    public MicrosoftParaphraser(String clientId, String clientSecret) {
        languages.add(Language.FRENCH);
        languages.add(Language.GERMAN);
        languages.add(Language.SPANISH);
        Translate.setClientId(clientId);
        Translate.setClientSecret(clientSecret);
    }

    @Override
    public List<String> getParaphrases(String question) {
        List<String> result = new ArrayList<String>();
        try {
            for (Language l : languages) {
                result.add(Translate.execute(Translate.execute(question, Language.ENGLISH, l), l, Language.ENGLISH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static void main(String args[])
    {
        MicrosoftParaphraser ms = new MicrosoftParaphraser("X", "Y");
        System.out.println(ms.getParaphrases("Give me all actors of the television series Charmed."));
    }
    
}
