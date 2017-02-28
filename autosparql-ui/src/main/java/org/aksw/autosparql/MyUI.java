package org.aksw.autosparql;

import javax.servlet.annotation.WebServlet;
import javax.swing.*;

import com.google.common.collect.Iterables;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.DetailsGenerator;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.themes.ValoTheme;
import de.fatalix.vaadin.addon.codemirror.CodeMirror;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorLanguage;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorTheme;
import org.aksw.jena_sparql_api.backports.syntaxtransform.ElementTransformer;
import org.aksw.jena_sparql_api.backports.syntaxtransform.TransformElementLib;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorRDFS;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.reasoning.SPARQLReasoner;
import org.vaadin.sliderpanel.SliderPanel;
import org.vaadin.sliderpanel.SliderPanelBuilder;
import org.vaadin.sliderpanel.SliderPanelStyles;
import org.vaadin.sliderpanel.client.SliderMode;
import org.vaadin.sliderpanel.client.SliderTabPosition;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    enum Dataset {
        DBPEDIA("DBpedia", "http://dbpedia.org",
                Lists.newArrayList("http://dbpedia.org/resource/Dresden",
                                   "http://dbpedia.org/resource/Leipzig"),
                Lists.newArrayList("dbo,http://dbpedia.org/ontology/",
                                   "dbp,http://dbpedia.org/property/",
                                   "dbr,http://dbpedia.org/resource/")),
        LINKEDMDB("LinkedMDB", "http://linkedmdb.org",
                  Lists.newArrayList("http://data.linkedmdb.org/resource/film/10283",
                                     "http://data.linkedmdb.org/resource/film/10459"),
                  Lists.newArrayList("film,http://data.linkedmdb.org/resource/film/",
                                     "movie,http://data.linkedmdb.org/resource/movie/")),
        BIOMEDICAL("Biomedical", "http://biomedical.org",
                   Lists.newArrayList("http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/1003",
                                      "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/1004"),
                   Lists.newArrayList("diseasome,http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/",
                                      "drugbank,http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/",
                                      "sider,http://www4.wiwiss.fu-berlin.de/sider/resource/sider/drugs"));

        String label;
        String uri;
        List<String> examples;
        PrefixMapping prefixes;

        Dataset(String label, String uri, List<String> examples, List<String> prefixesList) {
            this.label = label;
            this.uri = uri;
            this.examples = examples;

            prefixes = new PrefixMappingImpl();
            prefixes.withDefaultMappings(PrefixMapping.Standard);

            prefixesList.forEach(e -> {
                String[] split = e.split(",");
                String prefix = split[0];
                String ns = split[1];
                prefixes.setNsPrefix(prefix, ns);
            });
        }

        public String getLabel() {
            return label;
        }
    }

    TextArea inputField;
    TextArea inputField2;
    TextArea queryField;
    CodeMirror codeMirror;
    Slider maxDepthSlider;
    CheckBox inferenceCB;
    CheckBox minimizeCB;
    CheckBox inComingDataCB;
    ComboBox<Dataset> datasetSelector;
    Grid<String> grid;

    private static final String SPARQL_SERVICE_ENDPOINT = "http://172.18.160.4:8890/sparql";
    private static final String SPARQL_SERVICE_DEFAULT_GRAPH = "http://dbpedia.org";

    QueryExecutionFactory qef = FluentQueryExecutionFactory
            .http(SPARQL_SERVICE_ENDPOINT, SPARQL_SERVICE_DEFAULT_GRAPH)
            .config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
                    .setModelContentType(WebContent.contentTypeRDFXML))
            .end()
            .create();
    ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
    QueryTreeFactory qtf = new QueryTreeFactoryBase();
    AbstractReasonerComponent reasoner;

    Function<Query, QueryExecution> queryToQueryExecution = (query) -> qef.createQueryExecution(query);

    Function<String, Model> uriToCBD = (uri) -> cbdGen.getConciseBoundedDescription(uri, maxDepthSlider.getValue().intValue());

    Function<String, RDFResourceTree> uriToQueryTree = (uri) -> qtf.getQueryTree(uri, uriToCBD.apply(uri), maxDepthSlider.getValue().intValue());


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        // KB selector
        List<Dataset> datasets = Arrays.asList(Dataset.values());

        datasetSelector = new ComboBox<>("Select a dataset");
        datasetSelector.setItems(datasets);
        datasetSelector.setItemCaptionGenerator(Dataset::getLabel);
        datasetSelector.setEmptySelectionAllowed(false);
        datasetSelector.setSelectedItem(Dataset.DBPEDIA);
        datasetSelector.setWidth(100, Unit.PERCENTAGE);
        datasetSelector.addValueChangeListener((e) -> inputField.setValue(
                e.getValue().examples.stream()
                        .map(uri -> "<" + uri + ">")
                        .collect(Collectors.joining(" "))));
        layout.addComponent(datasetSelector);

        final HorizontalLayout top = new HorizontalLayout();
        top.setWidth(100f, Unit.PERCENTAGE);


        HorizontalLayout examplesPanel = new HorizontalLayout();

        inputField = new TextArea();
        inputField.setSizeFull();
        inputField.setValue("<http://dbpedia.org/resource/Dresden> <http://dbpedia.org/resource/Leipzig>");
        inputField.setCaption("Type the positive examples here:");

        inputField2 = new TextArea();
        inputField2.setSizeFull();
        inputField2.setValue("");
        inputField2.setCaption("Type the negative examples here:");

        examplesPanel.addComponentsAndExpand(inputField, inputField2);

        top.addComponentsAndExpand(examplesPanel);



        maxDepthSlider = new Slider();
        maxDepthSlider.setOrientation(SliderOrientation.VERTICAL);
        maxDepthSlider.addStyleName("ticks");
//        maxDepthSlider.setWidth("200px");
        maxDepthSlider.setHeight(100, Unit.PERCENTAGE);
        maxDepthSlider.setMin(1);
        maxDepthSlider.setMax(3);
        top.addComponent(maxDepthSlider);
        top.setComponentAlignment(maxDepthSlider, Alignment.MIDDLE_CENTER);

        Button button = new Button("Run");
        button.addClickListener( e -> {
            onComputeSPARQLQuery();
        });
        top.addComponent(button);
        top.setComponentAlignment(button, Alignment.MIDDLE_CENTER);

        VerticalLayout advancedOptions = new VerticalLayout();
        inferenceCB = new CheckBox();
        inferenceCB.setCaption("Use Inference");
        minimizeCB = new CheckBox();
        minimizeCB.setCaption("Minimize");
        inComingDataCB = new CheckBox();
        inComingDataCB.setCaption("Use Incoming Data");
        advancedOptions.addComponents(inferenceCB, minimizeCB, inComingDataCB);

        SliderPanel sliderPanel = new SliderPanelBuilder(advancedOptions)
                .caption("Advanced Options")
                .mode(SliderMode.RIGHT)
                .tabPosition(SliderTabPosition.MIDDLE)
                .style(SliderPanelStyles.COLOR_WHITE)
                .build();
        top.addComponent(sliderPanel);

        Component resultPanel = createResultPanel();

        layout.addComponentsAndExpand(top, resultPanel);
        layout.setExpandRatio(top, 0.3f);
        layout.setExpandRatio(resultPanel, 0.7f);

        setContent(layout);

        reasoner = new SPARQLReasoner(qef);
        reasoner.setPrecomputeClassHierarchy(true);
        try {
            reasoner.init();
        } catch (ComponentInitException e) {
            e.printStackTrace();
        }
    }

    private Component createResultPanel() {
        HorizontalLayout l = new HorizontalLayout();

        queryField = new TextArea();
        queryField.setSizeFull();
        queryField.setReadOnly(true);

        codeMirror = new CodeMirror();
        codeMirror.setWidth(100f, Unit.PERCENTAGE);
        codeMirror.setHeight(600, Unit.PIXELS);
        codeMirror.setReadOnly(true);
        codeMirror.setCode("");
//        codeMirror.setWidth(1000, Unit.PIXELS);
        codeMirror.setLanguage(CodeMirrorLanguage.SPARQL);
        codeMirror.setTheme(CodeMirrorTheme.DEFAULT);

        Panel panel = new Panel(codeMirror);
        panel.setSizeFull();

        grid = new Grid<>();
        grid.setDetailsGenerator(new DetailsGenerator<String>() {
            @Override
            public Component apply(String s) {

                VerticalLayout layout = new VerticalLayout();

                Label l = new Label("Details for " + s);
                return l;
            }}
            );
//        grid.setColumns("");
//        grid.setDataProvider(dataProvider);
        grid.addItemClickListener(e -> {
            if(e.getMouseEventDetails().isDoubleClick()) {
                String item = e.getItem();
                grid.setDetailsVisible(item, !grid.isDetailsVisible(item));
            }
        });

        grid.addColumn(String::toString);
        grid.setSizeFull();

        l.addComponentsAndExpand(panel, grid);

        return l;
    }

    private void onComputeSPARQLQuery() {

        // the selected dataset
        Dataset dataset = datasetSelector.getValue();
        String datasetURI = datasetSelector.getValue().uri;
        qef = FluentQueryExecutionFactory
                .http(SPARQL_SERVICE_ENDPOINT, datasetURI)
                .config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
                        .setModelContentType(WebContent.contentTypeRDFXML))
                .end()
                .create();
        if(inComingDataCB.getValue()) {
            cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(qef);
            qtf = new QueryTreeFactoryBaseInv();
        } else {
            cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
            qtf = new QueryTreeFactoryBase();
        }

        reasoner = new SPARQLReasoner(qef);
        reasoner.setPrecomputeClassHierarchy(true);
        try {
            reasoner.init();
        } catch (ComponentInitException e) {
            e.printStackTrace();
        }



        // parse the examples from the input field
        List<String> posExamples = parseExamples();
        System.out.println(posExamples);

        List<RDFResourceTree> posExampleTrees = posExamples.stream().map(uriToQueryTree).collect(Collectors.toList());

        LGGGenerator lggGen;
        if(inferenceCB.getValue()) {
            lggGen = new LGGGeneratorRDFS(reasoner);
        } else {
            lggGen = new LGGGeneratorSimple();
        }

        RDFResourceTree lgg = lggGen.getLGG(posExampleTrees);

//        QTL2Disjunctive qtl = new QTL2Disjunctive();

        String queryString = QueryTreeUtils.toSPARQLQueryString(lgg);

        Query query = QueryFactory.create(queryString);
        query.setPrefixMapping(dataset.prefixes);
        NodeTransformCollectNodes transform = new NodeTransformCollectNodes();
        ElementUtils.applyNodeTransform(query.getQueryPattern(), transform);
        Set<String> namespacesFound = new HashSet<>();
        transform.getNodes().stream().filter(Node::isURI).forEach(n -> {
            if(dataset.prefixes.qnameFor(n.getURI()) != null) {
                namespacesFound.add(n.getNameSpace());
            }
        });
        query.getPrefixMapping().getNsPrefixMap().entrySet().forEach(entry -> {
            if(!namespacesFound.contains(entry.getValue())) {
                query.getPrefixMapping().removeNsPrefix(entry.getKey());
            }
        });


        queryField.setValue(query.toString());

        codeMirror.setCode(query.toString());

        showResult(query);
    }

    /**
     * Generate a data provider for a SPARQL query.
     * @param q the SPARQL query
     */
    private void showResult(Query q) {
        DataProvider<String, Void> dataProvider = DataProvider.fromCallbacks(
                query -> {
                    Query copy = q.cloneQuery();
                    // The index of the first item to load
                    int offset = query.getOffset();

                    // The number of items to load
                    int limit = query.getLimit();

                    copy.setLimit(limit);
                    copy.setOffset(offset);

                    List<String> res = new ArrayList<>();

                    try(QueryExecution qe = qef.createQueryExecution(copy)) {
                        ResultSet rs = qe.execSelect();
                        while(rs.hasNext()) {
                            QuerySolution qs = rs.next();
                            res.add(qs.getResource("s").getURI());
                        }
                    }

                    return res.stream();
                },
                query -> {
                    Query copy = q.cloneQuery();
                    int cnt = 0;
                    Var vars = copy.getProject().getVars().iterator().next();
                    copy.setDistinct(false);
                    copy.getProject().clear();
                    Aggregator aggregator = AggregatorFactory.createCountExpr(true,
                                                                              new ExprVar(vars));
                    Expr expr = copy.allocAggregate(aggregator);
                    copy.addResultVar(Var.alloc("cnt"), expr);

                    System.out.println(copy);

                    try(QueryExecution qe = qef.createQueryExecution(copy)) {
                        ResultSet rs = qe.execSelect();
                        if(rs.hasNext()) {
                            cnt = rs.next().getLiteral("cnt").getInt();
                        }
                    }
                    return cnt;
                }
        );

        grid.setDataProvider(dataProvider);
    }

    private List<String> parseExamples() {
        List<String> posExamples = parseExamples(inputField.getValue());
        List<String> negExamples = parseExamples(inputField2.getValue());

        return posExamples;
    }

    private List<String> parseExamples(String text) {
        Pattern pattern = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");
        Matcher matcher = pattern.matcher(text);

        List<String> examples = new ArrayList<>();

        while (matcher.find()) {
            String uri = matcher.group();

            examples.add(uri);
        }

        return examples;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    public static void main(String[] args) throws Exception {
        String s = "+<http://ex.org#foo>   -<ftp://ex.org/bar> -";
        Pattern pattern = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");
        Matcher matcher = pattern.matcher(s);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }

        Query q = QueryFactory.create("PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
                                              "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                                              "PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                                              "PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" +
                                              "PREFIX  dbr:  <http://dbpedia.org/resource/>\n" +
                                              "\n" +
                                              "SELECT DISTINCT  ?s\n" +
                                              "WHERE\n" +
                                              "  { ?s  dbo:areaTotal         ?x0 ;\n" +
                                              "        dbo:country           dbr:Germany ;\n" +
                                              "        dbo:leaderName        ?x1 ;\n" +
                                              "        dbo:leaderTitle       ?x2 ;\n" +
                                              "        dbo:populationAsOf    ?x3 ;\n" +
                                              "        dbo:populationMetro   ?x4 ;\n" +
                                              "        rdf:type              dbo:Location, dbo:Place, dbo:PopulatedPlace ;\n" +
                                              "        rdfs:seeAlso          dbr:Germany ;\n" +
                                              "        rdfs:seeAlso          dbr:Lists_of_twin_towns_and_sister_cities ;\n" +
                                              "        rdfs:seeAlso          dbr:Twin_towns_and_sister_cities ;\n" +
                                              "        <http://xmlns.com/foaf/0.1/homepage>  ?x5\n" +
                                              "  }");
        q.setPrefixMapping(PrefixMapping.Extended);
        System.out.println(q.toString());


    }
}
