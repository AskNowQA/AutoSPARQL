/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.autosparql.algorithm.tbsl.paraphrase;

import java.util.List;

/**
 *
 * @author ngonga
 */
public interface Paraphraser {
    List<String> getParaphrases(String question);
}
