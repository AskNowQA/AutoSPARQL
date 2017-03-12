package org.aksw.autosparql;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.DetailsGenerator;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import de.fatalix.vaadin.addon.codemirror.CodeMirror;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorLanguage;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorTheme;
import org.aksw.autosparql.search.FulltextSearchSPARQLVirtuoso;
import org.aksw.autosparql.search.SearchResultExtended;
import org.aksw.autosparql.widget.NumberField;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.update.UpdateAction;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
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

import javax.servlet.annotation.WebServlet;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Title("AutoSPARQL")
@Theme("mytheme")
//@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
@Widgetset("org.aksw.autosparql.widgetset.AutoSPARQLWidgetSet")
public class MyUI extends UI {

    TextArea posExamplesInputField;
    TextArea negExamplesInputField;
    TextArea queryField;
    CodeMirror codeMirror;
    Slider maxDepthSlider;
    CheckBox inferenceCB;
    CheckBox minimizeCB;
    CheckBox inComingDataCB;
    ComboBox<Dataset> datasetSelector;
    Grid<RDFNode> grid;
    SPARQLBasedDataProvider dataProvider;

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
    FulltextSearchSPARQLVirtuoso fulltextSearch;

    Function<Query, QueryExecution> queryToQueryExecution = (query) -> qef.createQueryExecution(query);

    Function<String, Model> uriToCBD = (uri) -> cbdGen.getConciseBoundedDescription(uri, maxDepthSlider.getValue().intValue());

    Function<String, RDFResourceTree> uriToQueryTree = (uri) -> qtf.getQueryTree(uri, uriToCBD.apply(uri), maxDepthSlider.getValue().intValue());

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        // KB selector
        HorizontalLayout datasetSelectorAndSettings = new HorizontalLayout();
        datasetSelectorAndSettings.setWidth(100, Unit.PERCENTAGE);
        datasetSelectorAndSettings.addComponent(createDatasetSelector());
        datasetSelectorAndSettings.addComponent(createSettingsPanel());
        layout.addComponent(datasetSelectorAndSettings);

        final HorizontalLayout top = new HorizontalLayout();
        top.setWidth(100f, Unit.PERCENTAGE);

        top.addComponentsAndExpand(createExamplesInputPanel());

//        top.setComponentAlignment(maxDepthSlider, Alignment.MIDDLE_CENTER);

        Button button = new Button("Run");
        button.addClickListener( e -> {
            onComputeSPARQLQuery();
        });
        top.addComponent(button);
        top.setComponentAlignment(button, Alignment.MIDDLE_CENTER);


        Component resultPanel = createResultPanel();

        layout.addComponentsAndExpand(top, resultPanel);
        layout.setExpandRatio(top, 0.3f);
        layout.setExpandRatio(resultPanel, 0.7f);

        setContent(layout);

        setContent(new AutoSPARQLDesign());

        datasetSelector.setSelectedItem(Dataset.DBPEDIA);
    }

    private Component createDatasetSelector() {
        datasetSelector = new ComboBox<>(VaadinIcons.DATABASE.getHtml() + " Select a dataset");
        datasetSelector.setCaptionAsHtml(true);
        datasetSelector.setItems(Arrays.asList(Dataset.values()));
        datasetSelector.setItemCaptionGenerator(Dataset::getLabel);
        datasetSelector.setEmptySelectionAllowed(false);
        datasetSelector.setWidth(100, Unit.PERCENTAGE);
        datasetSelector.addValueChangeListener((e) -> onChangeDataset(e.getValue()));

        return datasetSelector;
    }

    private Component createAdvancedSettingsMenu() {
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

        return sliderPanel;
    }

    private Component createSettingsPanel() {
        FormLayout settings = new FormLayout();
        settings.setCaption(VaadinIcons.COG_O.getHtml() + " Settings");
        settings.setCaptionAsHtml(true);
        settings.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

        maxDepthSlider = new Slider();
        maxDepthSlider.setCaption("Max. Depth");
//        maxDepthSlider.setOrientation(SliderOrientation.VERTICAL);
        maxDepthSlider.addStyleName("ticks");
//        maxDepthSlider.setWidth("200px");
        maxDepthSlider.setHeight(100, Unit.PERCENTAGE);
        maxDepthSlider.setMin(1);
        maxDepthSlider.setMax(3);

        HorizontalLayout wrap = new HorizontalLayout();
        wrap.setSpacing(true);
        wrap.setCaption("Max. Execution Time");
        wrap.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        CheckBox useMaxExecutionTime = new CheckBox("Set max. execution time to", true);
        wrap.addComponent(useMaxExecutionTime);

        final TextField maxExecutionTimeField = new TextField();
        NumberField.extend(maxExecutionTimeField);
        maxExecutionTimeField.setValue("10");
        wrap.addComponent(maxExecutionTimeField);

        useMaxExecutionTime.addValueChangeListener((e) -> maxExecutionTimeField.setEnabled(e.getValue()));

        inferenceCB = new CheckBox();
        inferenceCB.setCaption("Use Inference");
        minimizeCB = new CheckBox();
        minimizeCB.setCaption("Minimize Query");
        inComingDataCB = new CheckBox();
        inComingDataCB.setCaption("Use Incoming Data");

        settings.addComponents(maxDepthSlider, wrap, inferenceCB, minimizeCB, inComingDataCB);

        Panel p = new Panel();
        p.setCaption(VaadinIcons.COG_O.getHtml() + " Settings");
        p.setCaptionAsHtml(true);
        p.addStyleName(ValoTheme.PANEL_BORDERLESS);
        p.setContent(settings);

        return p;
//        return settings;
    }

    private void onChangeDataset(Dataset dataset) {
        posExamplesInputField.setValue(
                dataset.examples.stream()
                        .map(uri -> "<" + uri + ">")
                        .collect(Collectors.joining(" ")));

        String datasetURI = datasetSelector.getValue().uri;
        qef = FluentQueryExecutionFactory
                .http(SPARQL_SERVICE_ENDPOINT, datasetURI)
                .config().withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee())
                        .setModelContentType(WebContent.contentTypeRDFXML))
                .end()
                .create();

        fulltextSearch = new FulltextSearchSPARQLVirtuoso(qef);
    }

    private Component createExamplesInputPanel() {
        HorizontalLayout examplesPanel = new HorizontalLayout();
        examplesPanel.setSizeFull();

//        VerticalLayout vl = new VerticalLayout();
//        vl.setSpacing(false);
//        vl.setMargin(false);
//        HorizontalLayout hhl = new HorizontalLayout();
//        hhl.setWidth(100f, Unit.PERCENTAGE);
//        hhl.setSpacing(false);
//        hhl.setMargin(false);
//        Label l = new Label("Type the positive examples here:");
//        hhl.addComponent(l);
//        Button search = new Button(VaadinIcons.SEARCH);
//        search.addStyleName(ValoTheme.BUTTON_LINK);
//        hhl.addComponent(search);
//        hhl.setComponentAlignment(search, Alignment.MIDDLE_LEFT);
//        vl.addComponent(hhl);

        posExamplesInputField = new TextArea();
        posExamplesInputField.setSizeFull();
        posExamplesInputField.setIcon(VaadinIcons.PLUS_CIRCLE_O);
        posExamplesInputField.setCaption("Type the positive examples here:");

        Button searchButton = new Button("Search", VaadinIcons.SEARCH);
        searchButton.addClickListener((e) -> {
            getCurrent().getUI().addWindow(createSearchDialog());
        });
//        searchButton.setSizeUndefined();

        negExamplesInputField = new TextArea();
        negExamplesInputField.setSizeFull();
        negExamplesInputField.setIcon(VaadinIcons.MINUS_CIRCLE_O);
        negExamplesInputField.setCaption("Type the negative examples here:");

//        vl.addComponent(hhl);
//        vl.addComponentsAndExpand(posExamplesInputField);

        examplesPanel.addComponentsAndExpand(posExamplesInputField);
//        examplesPanel.addComponent(searchButton);
        examplesPanel.addComponentsAndExpand(negExamplesInputField);

        return examplesPanel;
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
        grid.setSizeFull();
        grid.setDetailsGenerator(new DetailsGenerator<RDFNode>() {
            @Override
            public Component apply(RDFNode s) {

                VerticalLayout layout = new VerticalLayout();
                StringWriter sw = new StringWriter();
                describe(s.toString()).write(sw, "TURTLE");
                Label l = new Label(sw.toString(), ContentMode.PREFORMATTED);
                layout.addComponent(l);
                return layout;
            }}
            );
//        grid.setColumns("");
//        grid.setDataProvider(dataProvider);
        grid.addItemClickListener(e -> {
            if(e.getMouseEventDetails().isDoubleClick()) {
                RDFNode item = e.getItem();
                grid.setDetailsVisible(item, !grid.isDetailsVisible(item));
            }
        });

        grid.addColumn(RDFNode::toString);
//        grid.addColumn(s -> "Omit",
//                       new ButtonRenderer<>(clickEvent -> {
//                           RDFNode uri = clickEvent.getItem();
//                       negExamplesInputField.setValue(uri.toString());
//                   }));



        dataProvider = new SPARQLBasedDataProvider(qef);
        grid.setDataProvider(dataProvider);

        l.addComponentsAndExpand(panel, grid);

        return l;
    }

    private Window createSearchDialog() {
        final Window window = new Window("Search");
        window.setWidth(600.0f, Unit.PIXELS);
        window.setHeight(600.0f, Unit.PIXELS);

        final VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setMargin(true);

        HorizontalLayout hl = new HorizontalLayout();
        content.addComponent(hl);

        TextField searchField = new TextField("Enter search term");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        hl.addComponentsAndExpand(searchField);

        Button runButton = new Button("Search");
        hl.addComponent(runButton);
        hl.setComponentAlignment(runButton, Alignment.BOTTOM_CENTER);

        Grid<SearchResultExtended> resultGrid = new Grid<>();
        resultGrid.setStyleGenerator((e) -> "bigRows");
        resultGrid.addStyleName("resultGrid");
        resultGrid
                .addColumn(searchResult ->
                                   "<b>" + searchResult.getLabel() + "</b> (" + searchResult.getResource().getURI() + ")</br>" +
//                                           "<p class=\"wrap\">Types: " + searchResult.getTypes() + "</p>" +
                                           "<p class=\"wrap\">" + searchResult.getComment() + "</p>"
                        , new HtmlRenderer())
//                .addColumn(SearchResult::getResource)
                .setCaption("")
                .setStyleGenerator((e) -> "bigCell");

//            resultGrid.
//                    addComponentColumn("TEST", result ->
//                            new Label("<b>" + result.getResource().getURI() + "</b></br><p>" + result.getComment() + "</p>", ContentMode.HTML));
                                   resultGrid.setSizeFull();
//            resultGrid.setStyleGenerator(s -> "heigher");
        content.addComponentsAndExpand(resultGrid);

        runButton.addClickListener((e2) -> {
            String searchTerm = searchField.getValue();
            Stream<SearchResultExtended> result = search(searchTerm);
            resultGrid.setItems(result);
//                resultGrid.setRows(result.collect(Collectors.toList()));

        });

        searchField.addShortcutListener(new ShortcutListener("Enter", ShortcutAction.KeyCode.ENTER, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                String searchTerm = searchField.getValue();
                Stream<SearchResultExtended> result = search(searchTerm);
                resultGrid.setItems(result);
//                    resultGrid.setRows(result.collect(Collectors.toList()));
            }
        });
        window.setContent(content);

        return window;
    }

    private Model describe(String uri) {
        return qef.createQueryExecution("CONSTRUCT WHERE { <" + uri + "> ?p ?o }").execConstruct();
    }

    private void onComputeSPARQLQuery() {

        // the selected dataset
        Dataset dataset = datasetSelector.getValue();
        String datasetURI = datasetSelector.getValue().uri;

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

//        new Thread(() -> showResult(query)).start();
    }

    /**
     * Generate a data provider for a SPARQL query.
     * @param q the SPARQL query
     */
    private void showResult(Query q) {
        dataProvider.setQuery(q);
        dataProvider.refreshAll();
//        UI.getCurrent().access(() -> grid.setDataProvider(dataProvider));
    }

    private List<String> parseExamples() {
        List<String> posExamples = parseExamples(posExamplesInputField.getValue());
        List<String> negExamples = parseExamples(negExamplesInputField.getValue());

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

    /**
     *
     select ?s sample(?l) sample(?c) (GROUP_CONCAT(?type;separator=", ") as ?types)
     from <http://dbpedia.org>
     from <http://dbpedia.org/labels>
     from <http://dbpedia.org/comments>
     where {
     ?s <http://www.w3.org/2000/01/rdf-schema#label> ?l.
     ?l <bif:contains> "leipzig" .
     ?s rdf:type ?type .
     ?s rdfs:comment ?c .
     }
     GROUP BY ?s
     ORDER BY DESC(<LONG::IRI_RANK>(?s))
     LIMIT 100
     * @param s
     * @return
     */
    private Stream<SearchResultExtended> search(String s) {
       return fulltextSearch.search(s);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    public static void main(String[] args) throws Exception {
        String updateString = "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" +
                "PREFIX p: <http://www.wikidata.org/prop/>\n" +
                "PREFIX wd: <http://www.wikidata.org/entity/>\n" +
                "\n" +
                "INSERT \n" +
                "  { ?s ?p ?o }\n" +
                "WHERE\n" +
                "  { SERVICE  <https://query.wikidata.org/bigdata/namespace/wdq/sparql> #don't know what to insert here for \"GRAPH\" either\n" +
                "    {                           #a working example query for wikidata:\n" +
                "      ?s wdt:P31 wd:Q5.         #humans\n" +
                "      ?s wdt:P54 wd:Q43310.     #germans\n" +
                "      ?s wdt:P1344 wd:Q79859.   #part of world cup 2014\n" +
                "      ?s ?p ?o.\n" +
                "    }\n" +
                "  }";
        Model m = ModelFactory.createDefaultModel();
        org.apache.jena.query.Dataset dataset = DatasetFactory.create(m);
        UpdateAction.parseExecute(updateString, dataset);
        System.out.println(m.size());
//        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
//        String data = "@prefix : <http://ex.org/> . @prefix owl: <http://www.w3.org/2002/07/owl#> ." +
//                ":a a :A . :A a owl:Class ." +
//                ":b a :B . :B a owl:Class ." +
//                ":a :p :b ." +
//                ":p a owl:SymmetricProperty .";
//        model.read(new StringReader(data), null, "TURTLE");
//
//        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//        reasoner = reasoner.bindSchema(model);
//        InfModel infmodel = ModelFactory.createInfModel(reasoner, model);
//
////        infmodel.listStatements().toSet().forEach(System.out::println);
//
////        model.listIndividuals().toSet().forEach(System.out::println);
//        model.listIndividuals().toSet().forEach(i -> {
//            if(i.hasProperty(model.getObjectProperty("http://ex.org/p"))) {
//                System.out.println(i);
//            }
//        });



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
        q.addOrderBy(new E_IRI_Rank(new ExprVar("s")), Query.ORDER_DESCENDING);
        System.out.println(q.toString());


    }
}
