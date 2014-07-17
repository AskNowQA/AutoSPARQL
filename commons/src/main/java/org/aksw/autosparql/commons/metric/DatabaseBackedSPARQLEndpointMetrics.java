package org.aksw.autosparql.commons.metric;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.Property;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.SPARQLReasoner;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class DatabaseBackedSPARQLEndpointMetrics {

	private static final Logger log = Logger.getLogger(DatabaseBackedSPARQLEndpointMetrics.class.getSimpleName());

	private SparqlEndpoint endpoint;
	private SPARQLReasoner reasoner;
	private Connection connection;
	private ExtractionDBCache cache;
	private Statement stmt;

	private PreparedStatement subjectClassPredicateSelectPreparedStatement;
	private PreparedStatement subjectClassPredicateInsertPreparedStatement;
	private PreparedStatement predicateObjectClassSelectPreparedStatement;
	private PreparedStatement predicateObjectClassInsertPreparedStatement;
	private PreparedStatement subjectClassObjectClassSelectPreparedStatement;
	private PreparedStatement subjectClassObjectClassInsertPreparedStatement;
	private PreparedStatement subjectClassSelectPreparedStatement;
	private PreparedStatement subjectClassInsertPreparedStatement;
	private PreparedStatement objectClassSelectPreparedStatement;
	private PreparedStatement objectClassInsertPreparedStatement;
	private PreparedStatement classPopularitySelectPreparedStatement;
	private PreparedStatement classPopularityInsertPreparedStatement;
	private PreparedStatement propertyPopularitySelectPreparedStatement;
	private PreparedStatement propertyPopularityInsertPreparedStatement;
	private PreparedStatement connectingPropertiesSelectPreparedStatement;
	private PreparedStatement connectingPropertiesInsertPreparedStatement;

	public DatabaseBackedSPARQLEndpointMetrics(SparqlEndpoint endpoint, ExtractionDBCache cache, Connection connection) {
		this.endpoint = endpoint;
		this.connection = connection;
		this.cache = cache;
		this.reasoner = new SPARQLReasoner(new SparqlEndpointKS(endpoint));

		try {if(!connection.isReadOnly()) {createDatabaseTables();}
		} catch (SQLException e1) {	e1.printStackTrace();}

		try {
			subjectClassPredicateSelectPreparedStatement = connection.prepareStatement("SELECT OCCURRENCES FROM SUBJECTCLASS_PREDICATE_OCCURRENCES WHERE SUBJECTCLASS=? && PREDICATE=?");
			subjectClassPredicateInsertPreparedStatement = connection.prepareStatement("INSERT INTO SUBJECTCLASS_PREDICATE_OCCURRENCES (SUBJECTCLASS, PREDICATE, OCCURRENCES) VALUES(?, ?, ?)");

			predicateObjectClassSelectPreparedStatement = connection.prepareStatement("SELECT OCCURRENCES FROM PREDICATE_OBJECTCLASS_OCCURRENCES WHERE OBJECTCLASS=? && PREDICATE=?");
			predicateObjectClassInsertPreparedStatement = connection.prepareStatement("INSERT INTO PREDICATE_OBJECTCLASS_OCCURRENCES (OBJECTCLASS, PREDICATE, OCCURRENCES) VALUES(?, ?, ?)");

			subjectClassObjectClassSelectPreparedStatement= connection.prepareStatement("SELECT OCCURRENCES FROM SUBJECTCLASS_OBJECTCLASS_OCCURRENCES WHERE SUBJECTCLASS=? && OBJECTCLASS=?");
			subjectClassObjectClassInsertPreparedStatement = connection.prepareStatement("INSERT INTO SUBJECTCLASS_OBJECTCLASS_OCCURRENCES (SUBJECTCLASS, OBJECTCLASS, OCCURRENCES) VALUES(?, ?, ?)");

			subjectClassSelectPreparedStatement= connection.prepareStatement("SELECT OCCURRENCES FROM SUBJECTCLASS_OCCURRENCES WHERE SUBJECTCLASS=?");
			subjectClassInsertPreparedStatement = connection.prepareStatement("INSERT INTO SUBJECTCLASS_OCCURRENCES (SUBJECTCLASS, OCCURRENCES) VALUES(?, ?)");

			objectClassSelectPreparedStatement= connection.prepareStatement("SELECT OCCURRENCES FROM OBJECTCLASS_OCCURRENCES WHERE OBJECTCLASS=?");
			objectClassInsertPreparedStatement = connection.prepareStatement("INSERT INTO OBJECTCLASS_OCCURRENCES (OBJECTCLASS, OCCURRENCES) VALUES(?, ?)");

			classPopularitySelectPreparedStatement= connection.prepareStatement("SELECT POPULARITY FROM CLASS_POPULARITY WHERE CLASS=?");
			classPopularityInsertPreparedStatement = connection.prepareStatement("INSERT INTO CLASS_POPULARITY (CLASS, POPULARITY) VALUES(?, ?)");

			propertyPopularitySelectPreparedStatement= connection.prepareStatement("SELECT POPULARITY FROM PROPERTY_POPULARITY WHERE PROPERTY=?");
			propertyPopularityInsertPreparedStatement = connection.prepareStatement("INSERT INTO PROPERTY_POPULARITY (PROPERTY, POPULARITY) VALUES(?, ?)");

			connectingPropertiesSelectPreparedStatement= connection.prepareStatement("SELECT PROPERTY, OCCURRENCES FROM CLASS_CONNECTING_PROPERTIES WHERE SUBJECTCLASS=? && OBJECTCLASS=?");
			connectingPropertiesInsertPreparedStatement = connection.prepareStatement("INSERT INTO CLASS_CONNECTING_PROPERTIES (SUBJECTCLASS, OBJECTCLASS, PROPERTY, OCCURRENCES) VALUES(?, ?, ?, ?)");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void createDatabaseTables(){
		try {
			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS SUBJECTCLASS_PREDICATE_OCCURRENCES(SUBJECTCLASS VARCHAR(100), PREDICATE VARCHAR(100), OCCURRENCES INTEGER, PRIMARY KEY(SUBJECTCLASS, PREDICATE))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS PREDICATE_OBJECTCLASS_OCCURRENCES(PREDICATE VARCHAR(100), OBJECTCLASS VARCHAR(100), OCCURRENCES INTEGER, PRIMARY KEY(OBJECTCLASS, PREDICATE))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS SUBJECTCLASS_OBJECTCLASS_OCCURRENCES(SUBJECTCLASS VARCHAR(100), OBJECTCLASS VARCHAR(100), OCCURRENCES INTEGER, PRIMARY KEY(SUBJECTCLASS, OBJECTCLASS))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS SUBJECTCLASS_OCCURRENCES(SUBJECTCLASS VARCHAR(100), OCCURRENCES INTEGER, PRIMARY KEY(SUBJECTCLASS))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS OBJECTCLASS_OCCURRENCES(OBJECTCLASS VARCHAR(100), OCCURRENCES INTEGER, PRIMARY KEY(OBJECTCLASS))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS PROPERTY_POPULARITY(PROPERTY VARCHAR(100), POPULARITY INTEGER, PRIMARY KEY(PROPERTY))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS CLASS_POPULARITY(CLASS VARCHAR(100), POPULARITY INTEGER, PRIMARY KEY(CLASS))");

			stmt = connection.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS CLASS_CONNECTING_PROPERTIES(SUBJECTCLASS VARCHAR(100), OBJECTCLASS VARCHAR(100), PROPERTY VARCHAR(100), OCCURRENCES INTEGER)");//, PRIMARY KEY(SUBJECTCLASS, OBJECTCLASS, PROPERTY))");

		} catch (SQLException e) {
			e.printStackTrace();
		}		

	}

	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(prop, cls) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getDirectedPMI(ObjectProperty prop, NamedClass cls){
		log.debug(String.format("Computing PMI(%s, %s)", prop, cls));

		double classOccurenceCnt = getOccurencesInObjectPosition(cls);
		double propertyOccurenceCnt = getOccurences(prop);
		double coOccurenceCnt = getOccurencesPredicateObject(prop, cls);
		double total = getTotalTripleCount();

		double pmi = 0;
		if(coOccurenceCnt > 0 && classOccurenceCnt > 0 && propertyOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		}
		log.debug(String.format("PMI(%s, %s) = %f", prop, cls, pmi));
		return pmi;
	}

	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(cls,prop) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getDirectedPMI(NamedClass cls, Property prop){
		log.debug(String.format("Computing PMI(%s, %s)...", cls, prop));

		double classOccurenceCnt = getOccurencesInSubjectPosition(cls);
		double propertyOccurenceCnt = getOccurences(prop);
		double coOccurenceCnt = getOccurencesSubjectPredicate(cls, prop);
		double total = getTotalTripleCount();

		double pmi = 0;
		if(coOccurenceCnt > 0 && classOccurenceCnt > 0 && propertyOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (classOccurenceCnt * propertyOccurenceCnt) );
		}
		log.debug(String.format("PMI(%s, %s) = %f", cls, prop, pmi));
		return pmi;
	}

	/**
	 * Computes the directed Pointwise Mutual Information(PMI) measure. Formula: log( (f(cls,prop) * N) / (f(cls) * f(prop) ) )
	 * @param cls
	 * @param prop
	 * @return
	 */
	public double getPMI(NamedClass subject, NamedClass object){
		log.debug(String.format("Computing PMI(%s, %s)", subject, object));

		double coOccurenceCnt = getOccurencesSubjectObject(subject, object);
		double subjectOccurenceCnt = getOccurencesInSubjectPosition(subject);
		double objectOccurenceCnt = getOccurencesInObjectPosition(object);
		double total = getTotalTripleCount();

		double pmi = 0;
		if(coOccurenceCnt > 0 && subjectOccurenceCnt > 0 && objectOccurenceCnt > 0){
			pmi = Math.log( (coOccurenceCnt * total) / (subjectOccurenceCnt * objectOccurenceCnt) );
		}
		log.debug(String.format("PMI(%s, %s) = %f", subject, object, pmi));
		return pmi;
	}

	/**
	 * Returns the direction of the given triple, computed by calculating the PMI values of each combination.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return -1 if the given triple should by reversed, else 1.
	 */
	public int getDirection(NamedClass subject, ObjectProperty predicate, NamedClass object){
		log.info(String.format("Computing direction between [%s, %s, %s]", subject, predicate, object));
		double pmi_obj_pred = getDirectedPMI(object, predicate);
		double pmi_pred_subj = getDirectedPMI(predicate, subject);
		double pmi_subj_pred = getDirectedPMI(subject, predicate);
		double pmi_pred_obj = getDirectedPMI(predicate, object);

		double threshold = 2.0;

		double value = ((pmi_obj_pred + pmi_pred_subj) - (pmi_subj_pred + pmi_pred_obj));
		log.info("(PMI(OBJECT, PREDICATE) + PMI(PREDICATE, SUBJECT)) - (PMI(SUBJECT, PREDICATE) + PMI(PREDICATE, OBJECT)) = " + value);

		if( value > threshold){
			log.info(object + "---" + predicate + "--->" + subject);
			return -1;
		} else {
			log.info(subject + "---" + predicate + "--->" + object);
			return 1;
		}
	}

	public Map<ObjectProperty, Integer> getMostFrequentProperties(NamedClass subjectClass, NamedClass objectClass){
		Map<ObjectProperty, Integer> property2Frequency= new HashMap<ObjectProperty, Integer>();
		try {
			connectingPropertiesSelectPreparedStatement.setString(1, subjectClass.getName());
			connectingPropertiesSelectPreparedStatement.setString(2, objectClass.getName());
			java.sql.ResultSet sqlResultset = connectingPropertiesSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				ObjectProperty p = new ObjectProperty(sqlResultset.getString(1));
				int frequency = sqlResultset.getInt(2);
				property2Frequency.put(p, frequency);
				while(sqlResultset.next()){
					p = new ObjectProperty(sqlResultset.getString(1));
					frequency = sqlResultset.getInt(2);
					property2Frequency.put(p, frequency);
				}
			} else {
				log.trace(String.format("Computing properties + frequency connecting subject of type %s and object of type %s", subjectClass.getName(), objectClass.getName()));
				String query = String.format("SELECT ?p (COUNT(*) AS ?cnt) WHERE {?x1 a <%s>. ?x2 a <%s>. ?x1 ?p ?x2} GROUP BY ?p", subjectClass, objectClass);
				ResultSet rs = executeSelect(query);
				QuerySolution qs;
				while(rs.hasNext()){
					qs = rs.next();
					ObjectProperty p = new ObjectProperty(qs.getResource("p").getURI());
					int cnt = qs.getLiteral("cnt").getInt();
					property2Frequency.put(p, cnt);
					if(!connection.isReadOnly())
					{
						connectingPropertiesInsertPreparedStatement.setString(1, subjectClass.getName());
						connectingPropertiesInsertPreparedStatement.setString(2, objectClass.getName());
						connectingPropertiesInsertPreparedStatement.setString(3, p.getName());
						connectingPropertiesInsertPreparedStatement.setInt(4, cnt);
						connectingPropertiesInsertPreparedStatement.executeUpdate();
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return property2Frequency;
	}

	/**
	 * Returns the number of triples with the given property as predicate and where the subject belongs to the given class.
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurencesSubjectPredicate(NamedClass cls, Property prop){
		try {
			subjectClassPredicateSelectPreparedStatement.setString(1, cls.getName());
			subjectClassPredicateSelectPreparedStatement.setString(2, prop.getName());
			java.sql.ResultSet sqlResultset = subjectClassPredicateSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of occurrences as subject and predicate for [%s, %s]", cls.getName(), prop.getName()));
				String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s <%s> ?o}", cls.getName(), prop.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					subjectClassPredicateInsertPreparedStatement.setString(1, cls.getName());
					subjectClassPredicateInsertPreparedStatement.setString(2, prop.getName());
					subjectClassPredicateInsertPreparedStatement.setInt(3, cnt);
					subjectClassPredicateInsertPreparedStatement.executeUpdate();				
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}


	/**
	 * Returns the number of triples with the given property as predicate and where the object belongs to the given class.
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurencesPredicateObject(Property prop, NamedClass cls){
		try {
			predicateObjectClassSelectPreparedStatement.setString(1, cls.getName());
			predicateObjectClassSelectPreparedStatement.setString(2, prop.getName());
			java.sql.ResultSet sqlResultset = predicateObjectClassSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of occurences as predicate and object for [%s, %s]", prop.getName(), cls.getName()));
				String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?o a <%s>. ?s <%s> ?o}", cls.getName(), prop.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					predicateObjectClassInsertPreparedStatement.setString(1, cls.getName());
					predicateObjectClassInsertPreparedStatement.setString(2, prop.getName());
					predicateObjectClassInsertPreparedStatement.setInt(3, cnt);
					predicateObjectClassInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the number of triples with the first given class as subject and the second given class as object.
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurencesSubjectObject(NamedClass subject, NamedClass object){
		try {
			subjectClassObjectClassSelectPreparedStatement.setString(1, subject.getName());
			subjectClassObjectClassSelectPreparedStatement.setString(2, object.getName());
			java.sql.ResultSet sqlResultset = subjectClassObjectClassSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of occurences as subject and object for [%s, %s]", subject.getName(), object.getName()));
				String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s a <%s>. ?s ?p ?o. ?o a <%s>}", subject.getName(), object.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					subjectClassObjectClassInsertPreparedStatement.setString(1, subject.getName());
					subjectClassObjectClassInsertPreparedStatement.setString(2, object.getName());
					subjectClassObjectClassInsertPreparedStatement.setInt(3, cnt);
					subjectClassObjectClassInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the number of triples where the subject belongs to the given class.
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurencesInSubjectPosition(NamedClass cls){
		try {
			subjectClassSelectPreparedStatement.setString(1, cls.getName());
			java.sql.ResultSet sqlResultset = subjectClassSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of triples where subject is of type %s", cls.getName()));
				String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>. ?s ?p ?o.}", cls.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					subjectClassInsertPreparedStatement.setString(1, cls.getName());
					subjectClassInsertPreparedStatement.setInt(2, cnt);
					subjectClassInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the number of triples where the object belongs to the given class.
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurencesInObjectPosition(NamedClass cls){
		try {
			objectClassSelectPreparedStatement.setString(1, cls.getName());
			java.sql.ResultSet sqlResultset = objectClassSelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of triples where object is of type %s", cls.getName()));
				String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?o a <%s>. ?s ?p ?o.}", cls.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					objectClassInsertPreparedStatement.setString(1, cls.getName());
					objectClassInsertPreparedStatement.setInt(2, cnt);
					objectClassInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the number triples with the given property as predicate.
	 * @param prop
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurences(Property prop){
		try {
			propertyPopularitySelectPreparedStatement.setString(1, prop.getName());
			java.sql.ResultSet sqlResultset = propertyPopularitySelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of occurences as predicate for %s", prop.getName()));
				String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s <%s> ?o}", prop.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					propertyPopularityInsertPreparedStatement.setString(1, prop.getName());
					propertyPopularityInsertPreparedStatement.setInt(2, cnt);
					propertyPopularityInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the number of triples where the subject or object belongs to the given class.
	 * (This is not the same as computing the number of instances of the given class {@link DatabaseBackedSPARQLEndpointMetrics#getPopularity(NamedClass)})
	 * @param cls
	 * @return
	 * @throws SQLException 
	 */
	public int getOccurences(NamedClass cls){
		try {
			classPopularitySelectPreparedStatement.setString(1, cls.getName());
			java.sql.ResultSet sqlResultset = classPopularitySelectPreparedStatement.executeQuery();
			if(sqlResultset.next()){
				return sqlResultset.getInt(1);
			} else {
				log.trace(String.format("Computing number of instances of class %s", cls.getName()));
				String query  = String.format("SELECT (COUNT(?s) AS ?cnt) WHERE {?s a <%s>.}", cls.getName());
				ResultSet rs = executeSelect(query);
				int cnt = rs.next().getLiteral("cnt").getInt();
				if (!connection.isReadOnly())
				{
					classPopularityInsertPreparedStatement.setString(1, cls.getName());
					classPopularityInsertPreparedStatement.setInt(2, cnt);
					classPopularityInsertPreparedStatement.executeUpdate();
				}
				return cnt;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Returns the total number of triples in the endpoint. For now we return a fixed number 275494030(got from DBpedia Live 18. July 14:00).
	 * @return
	 */
	public int getTotalTripleCount(){
		return 275494030;
		/*String query  = String.format("SELECT (COUNT(*) AS ?cnt) WHERE {?s ?p ?o}");
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int cnt = rs.next().getLiteral("cnt").getInt();
		return cnt;*/
	}

	public double getGoodness(NamedClass subject, ObjectProperty predicate, NamedClass object){
		log.info(String.format("Computing goodness of [%s, %s, %s]", subject.getName(), predicate.getName(), object.getName()));
		double pmi_subject_predicate = getDirectedPMI(subject, predicate);
		double pmi_preciate_object = getDirectedPMI(predicate, object);
		double pmi_subject_object = getPMI(subject, object);

		double goodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
		log.info(String.format("Goodness of [%s, %s, %s]=%f", subject.getName(), predicate.getName(), object.getName(), Double.valueOf(goodness)));
		return goodness;
	}

	public double getGoodness(Individual subject, ObjectProperty predicate, NamedClass object){
		log.info(String.format("Computing goodness of [%s, %s, %s]", subject.getName(), predicate.getName(), object.getName()));
		//this is independent of the subject types
		double pmi_preciate_object = getDirectedPMI(predicate, object);

		double goodness = Double.MIN_VALUE;
		//get all asserted classes of subject and get the highest value
		//TODO inference
		Set<NamedClass> types = reasoner.getTypes(subject);
		for(NamedClass type : types){
			if(!type.getName().startsWith("http://dbpedia.org/ontology/"))continue;
			double pmi_subject_predicate = getDirectedPMI(type, predicate);
			double pmi_subject_object = getPMI(type, object);
			double tmpGoodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
			if(tmpGoodness >= goodness){
				goodness = tmpGoodness;
			}
		}
		log.info(String.format("Goodness of [%s, %s, %s]=%f", subject.getName(), predicate.getName(), object.getName(), Double.valueOf(goodness)));
		return goodness;
	}

	public double getGoodness(NamedClass subject, ObjectProperty predicate, Individual object){
		log.info(String.format("Computing goodness of [%s, %s, %s]", subject.getName(), predicate.getName(), object.getName()));
		//this is independent of the object types
		double pmi_subject_predicate = getDirectedPMI(subject, predicate);

		double goodness = Double.MIN_VALUE;
		//get all asserted classes of subject and get the highest value
		//TODO inference
		Set<NamedClass> types = reasoner.getTypes(object);
		for(NamedClass type : types){
			if(!type.getName().startsWith("http://dbpedia.org/ontology/"))continue;
			double pmi_preciate_object = getDirectedPMI(predicate, type);
			double pmi_subject_object = getPMI(subject, type);
			double tmpGoodness = pmi_subject_predicate + pmi_preciate_object + 2*pmi_subject_object;
			if(tmpGoodness >= goodness){
				goodness = tmpGoodness;
			}
		}
		log.info(String.format("Goodness of [%s, %s, %s]=%f", subject.getName(), predicate.getName(), object.getName(), Double.valueOf(goodness)));
		return goodness;
	}

	public double getGoodnessConsideringSimilarity(NamedClass subject, ObjectProperty predicate, NamedClass object, 
			double subjectSim, double predicateSim, double objectSim){

		double pmi_subject_predicate = getDirectedPMI(subject, predicate);
		double pmi_preciate_object = getDirectedPMI(predicate, object);
		double pmi_subject_object = getPMI(subject, object);

		double goodness = pmi_subject_predicate * subjectSim * predicateSim
				+ pmi_preciate_object * objectSim * predicateSim
				+ 2 * pmi_subject_object * subjectSim * objectSim;

		return goodness;
	}

	public void precompute(){
		precompute(Collections.<String>emptySet());
	}

	public void precompute(Collection<String> namespaces){
		log.info("Precomputing...");
		long startTime = System.currentTimeMillis();
		SortedSet<NamedClass> classes = new TreeSet<NamedClass>();
		String query = "SELECT DISTINCT ?class WHERE {?s a ?class.";
		for(String namespace : namespaces){
			query += "FILTER(REGEX(STR(?class),'" + namespace + "'))";
		}
		query += "}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			classes.add(new NamedClass(qs.getResource("class").getURI()));
		}

		SortedSet<ObjectProperty> objectProperties = new TreeSet<ObjectProperty>();
		query = "SELECT DISTINCT ?prop WHERE {?prop a owl:ObjectProperty. ";
		for(String namespace : namespaces){
			query += "FILTER(REGEX(STR(?prop),'" + namespace + "'))";
		}
		query += "}";
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		while(rs.hasNext()){
			qs = rs.next();
			objectProperties.add(new ObjectProperty(qs.getResource("prop").getURI()));
		}

		for(NamedClass cls : classes){
			for(ObjectProperty prop : objectProperties){
				log.info("Processing class " + cls + " and property " + prop);
				try {
					getDirectedPMI(cls, prop);
					getDirectedPMI(prop, cls);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					try {
						getDirectedPMI(cls, prop);
						getDirectedPMI(prop, cls);
					} catch (Exception e2) {
						e2.printStackTrace();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}

			}
		}

		for(NamedClass cls1 : classes){
			for(NamedClass cls2 : classes){
				if(!cls1.equals(cls2)){
					log.info("Processing class " + cls1 + " and class " + cls2);
					try {
						getPMI(cls1, cls2);
						getPMI(cls2, cls1);
					} catch (Exception e) {
						e.printStackTrace();
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
						try {
							getPMI(cls1, cls2);
							getPMI(cls2, cls1);
						} catch (Exception e2) {
							e2.printStackTrace();
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}

		for(NamedClass cls1 : classes){
			for(NamedClass cls2 : classes){
				if(!cls1.equals(cls2)){
					log.info("Computing most frequent properties between class " + cls1 + " and class " + cls2);
					try {
						getMostFrequentProperties(cls1, cls2);
					} catch (Exception e) {
						log.error(e);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {
							log.error(e1);
						}
						try {
							getMostFrequentProperties(cls1, cls2);
						} catch (Exception e2) {
							log.error(e2);
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e1) {

							}
						}
					}
				}
			}
		}
		log.info("Done in " + ((System.currentTimeMillis() - startTime)/1000d) + "s");
	}

	private ResultSet executeSelect(String query){
		return SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		//		QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		//		qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
		//		return qe.execSelect();
	}

	public static void main(String[] args) throws Exception {
		//create database connection
		Class.forName("com.mysql.jdbc.Driver");
		String dbHost = "localhost";
		String dbPort = "3306";
		String database = "dbpedia_metrics";
		String dbUser = "root";
		String dbPassword = args[0];
		Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"
				+ dbPort + "/" + database + "?" + "user=" + dbUser + "&"
				+ "password=" + dbPassword);


		Logger.getLogger(DatabaseBackedSPARQLEndpointMetrics.class).setLevel(Level.TRACE);
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://linkedspending.aksw.org/sparql"), "http://dbpedia.org");
		ExtractionDBCache cache = new ExtractionDBCache("/tmp");
		String NS = "http://dbpedia.org/ontology/";		
//		new DatabaseBackedSPARQLEndpointMetrics(endpoint, cache, conn).precompute(Collections.singleton(NS));
	}

}