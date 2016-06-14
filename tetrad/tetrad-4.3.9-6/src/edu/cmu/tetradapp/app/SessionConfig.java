///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 2005 by Peter Spirtes, Richard Scheines, Joseph Ramsey,     //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetradapp.app;

import edu.cmu.tetrad.bayes.DirichletBayesIm;
import edu.cmu.tetrad.bayes.MlBayesIm;
import edu.cmu.tetrad.bayes.UpdaterParams;
import edu.cmu.tetrad.gene.graph.ManualActiveLagGraph;
import edu.cmu.tetrad.gene.graph.ManualLagGraphParams;
import edu.cmu.tetrad.gene.graph.StoredLagGraphParams;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.session.SessionModel;
import edu.cmu.tetradapp.editor.*;
import edu.cmu.tetradapp.gene.editor.*;
import edu.cmu.tetradapp.model.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.MultiKeyMap;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Makes the configuration of the application available as a singleton class.
 * The intention is for the maps of this class to be moved out to an XML file.
 *
 * @author Joseph Ramsey jdramsey@andrew.cmu.edu
 */
public final class SessionConfig {

    /**
     * Singleton instance.
     */
    private static final SessionConfig sessionConfig = new SessionConfig();

    /**
     * The map from button names to model class arrays.
     */
    private Map<String, Class[]> modelClassesMap;

    /**
     * The map from model classes to editor classes.
     */
    private Map<Class, Class> editorMap;

    /**
     * The map from model classes to their parameterizing classes.
     */
    private MultiKeyMap paramsMap;

    /**
     * The map from model classes to their descriptive names.
     */
    private Map<Class, String> descripMap;

    /**
     * The map from model classes to their acronyms.
     */
    private Map<Class, String> acronymMap;

    /**
     * The map from model classes to their acronyms.
     */
    private Map<Class, String> helpNameMap;

    /**
     * The map from button names to session node images.
     */
    private Map<String, String> sessionNodeImagesMap;

    /**
     * The map from button names to tooltips.
     */
    private Map<String, String> sessionNodeTooltipsMap;

    /**
     * Map from button names to titles for model choosers.
     */
    private Map<String, String> modelChooserTitlesMap;

    /**
     * Private constructor to initialize all the maps.
     */
    private SessionConfig() {
        initializeModelClassesMap();
        initializeEditorMap();
        initializeParamsMap();
        initializeDescripMap();
        initializeAcronymMap();
        initializeHelpNameMap();
        initializeSessionNodeImagesMap();
        initializeSessionNodeTooltipsMap();
        initializeModelChooserTitlesMap();
    }

    /**
     * Returns an instance of the session config with all maps initialized.
     */
    public static SessionConfig getInstance() {
        return sessionConfig;
    }

    /**
     * Initializes the map from session node button labels to the model classes
     * that are to be used to create new session nodes for buttons with those
     * labels. The sets of models for the various classes must be disjoint.
     */
    private void initializeModelClassesMap() {

        // Graph
        List<Class> graphClasses = new LinkedList<Class>();
        graphClasses.add(DagWrapper.class);
        graphClasses.add(SemGraphWrapper.class);
        graphClasses.add(GraphWrapper.class);
//        graphClasses.add(CompletedPatternWrapper.class);
//        graphClasses.add(RandomActiveLagGraph.class);

        // PM
        List<Class> pmClasses = new LinkedList<Class>();
        pmClasses.add(BayesPmWrapper.class);
        pmClasses.add(SemPmWrapper.class);
//        pmClasses.add(BooleanGlassGenePm.class);

        // IM
        List<Class> imClasses = new LinkedList<Class>();
        imClasses.add(BayesImWrapper.class);
        imClasses.add(DirichletBayesImWrapper.class);
        imClasses.add(SemImWrapper.class);
//        imClasses.add(BooleanGlassGeneIm.class);

        // Data
        List<Class> dataClasses = new LinkedList<Class>();
        dataClasses.add(DataWrapper.class);
        dataClasses.add(BayesDataWrapper.class);
        dataClasses.add(DirichletBayesDataWrapper.class);
        dataClasses.add(SemDataWrapper.class);
        dataClasses.add(BootstrapSamplerWrapper.class);
        dataClasses.add(GeneSimDataWrapper.class);

        // Estimator
        List<Class> estimatorClasses = new LinkedList<Class>();
        estimatorClasses.add(BayesEstimatorWrapper.class);
        estimatorClasses.add(SemEstimatorWrapper.class);
        estimatorClasses.add(DirichletEstimatorWrapper.class);
        estimatorClasses.add(EmBayesEstimatorWrapper.class);

        // Updater
        List<Class> updaterClasses = new LinkedList<Class>();
        updaterClasses.add(ApproximateUpdaterWrapper.class);
        updaterClasses.add(RowSummingExactWrapper.class);
        updaterClasses.add(CptInvariantUpdaterWrapper.class);
        updaterClasses.add(SemUpdaterWrapper.class);

        // Search
        List<Class> searchClasses = new LinkedList<Class>();
        searchClasses.add(PcRunner.class);
        searchClasses.add(CpcRunner.class);
        searchClasses.add(PcdRunner.class);
        searchClasses.add(FciRunner.class);
        searchClasses.add(CfciRunner.class);
        searchClasses.add(CcdRunner.class);
        searchClasses.add(GesRunner.class);
//        searchClasses.add(PcxRunner.class);
        searchClasses.add(MbfsRunner.class);
        searchClasses.add(CeFanSearchRunner.class);
//        searchClasses.add(StructEmBayesSearchRunner.class);
        searchClasses.add(MimBuildRunner.class);
        searchClasses.add(BuildPureClustersRunner.class);
        searchClasses.add(PurifyRunner.class);

        // Regression
        List<Class> regressionClasses = new LinkedList<Class>();
        regressionClasses.add(RegressionRunner.class);
        regressionClasses.add(LogisticRegressionRunner.class);

        // Classifier
        List<Class> classifierClasses = new LinkedList<Class>();
        classifierClasses.add(BayesUpdaterClassifierWrapper.class);

        // Compare
        List<Class> compareClasses = new LinkedList<Class>();
        compareClasses.add(GraphComparison.class);

        // Predict
        List<Class> predictClasses = new LinkedList<Class>();
        predictClasses.add(PredictionRunner.class);

        // Note
        List<Class> noteClasses = new LinkedList<Class>();
        noteClasses.add(NoteModel.class);

        this.modelClassesMap = new HashMap<String, Class[]>();

        getModelClassesMap().put("Graph", asArray(graphClasses));
        getModelClassesMap().put("PM", asArray(pmClasses));
        getModelClassesMap().put("IM", asArray(imClasses));
        getModelClassesMap().put("Data", asArray(dataClasses));
        getModelClassesMap().put("Estimator", asArray(estimatorClasses));
        getModelClassesMap().put("Updater", asArray(updaterClasses));
        getModelClassesMap().put("Classify", asArray(classifierClasses));
        getModelClassesMap().put("Search", asArray(searchClasses));
        getModelClassesMap().put("Compare", asArray(compareClasses));
        getModelClassesMap().put("Regression", asArray(regressionClasses));
        getModelClassesMap().put("Predict", asArray(predictClasses));
        getModelClassesMap().put("Note", asArray(noteClasses));

        for (Object o : getModelClassesMap().keySet()) {
            String type = (String) o;
            Class[] classes = getModelClassesMap().get(type);
            for (Class clazz : classes) {
                if (!(SessionModel.class.isAssignableFrom(clazz))) {
                    throw new IllegalArgumentException("Session model class " +
                            "does not implement SessionModel: " + clazz);
                }
            }
        }
    }

    private Class[] asArray(List<Class> graphClasses) {
        return graphClasses.toArray(new Class[0]);
    }

    /**
     * Initializes the map from model classes to editor classes.
     */
    private void initializeEditorMap() {
        this.editorMap = new HashMap<Class, Class>();

        // Graph
        getEditorMap().put(DagWrapper.class, DagEditor.class);
        getEditorMap().put(SemGraphWrapper.class, SemGraphEditor.class);
        getEditorMap().put(GraphWrapper.class, GraphEditor.class);
        getEditorMap().put(CompletedPatternWrapper.class, GraphEditor.class);
        getEditorMap().put(ManualActiveLagGraph.class, LagGraphEditor.class);
        getEditorMap().put(RandomActiveLagGraph.class, LagGraphEditor.class);

        getEditorMap().put(GraphParams.class, GraphParamsEditor.class);
        getEditorMap().put(LagGraphParams.class, LagGraphParamsEditor.class);
        getEditorMap().put(ManualLagGraphParams.class,
                ManualLagGraphParamsEditor.class);
        getEditorMap().put(StoredLagGraphParams.class,
                StoredLagGraphParamsEditor.class);

        // PM
        getEditorMap().put(BayesPmWrapper.class, BayesPmEditor.class);
        getEditorMap().put(SemPmWrapper.class, SemPmEditor.class);
        getEditorMap().put(BooleanGlassGenePm.class,
                BooleanGlassGenePmEditor.class);

        getEditorMap().put(BayesPmParams.class, BayesPmParamsEditor.class);

        // IM
        getEditorMap().put(BayesImWrapper.class, BayesImEditor.class);
        getEditorMap().put(DirichletBayesImWrapper.class,
                DirichletBayesImEditor.class);
        getEditorMap().put(SemImWrapper.class, SemImEditor.class);
        getEditorMap().put(BooleanGlassGeneIm.class,
                BooleanGlassGeneImEditor.class);

        getEditorMap().put(BayesImParams.class, BayesImParamsEditor.class);
        getEditorMap().put(DirichletBayesImParams.class,
                DirichletBayesImParamsEditor.class);
        getEditorMap().put(DirichletEstimatorParams.class,
                DirichletEstimatorParamsEditor.class);
        getEditorMap().put(EmBayesEstimatorParams.class,
                EMBayesEstimatorParamsEditor.class);
        getEditorMap().put(StructEmBayesSearchParams.class,
                StructEMBayesSearchParamsEditor.class);
    //    getEditorMap().put(RegressionParams.class,
    //            RegressionParamsEditor.class);
   //     getEditorMap().put(LogisticRegressionParams.class,
    //           LogisticRegressionParamsEditor.class);

        // Data
        getEditorMap().put(BayesDataWrapper.class, DataEditor.class);
        getEditorMap().put(DirichletBayesDataWrapper.class, DataEditor.class);
        //getEditorMap().put(EMBayesDataWrapper.class, DataEditor.class);
        getEditorMap().put(SemDataWrapper.class, DataEditor.class);
        getEditorMap().put(DataWrapper.class, DataEditor.class);
        getEditorMap().put(GeneSimDataWrapper.class, GeneDataEditor.class);
        getEditorMap().put(VariableSubsetterWrapper.class, DataEditor.class);
        getEditorMap().put(ModeInterpolatorWrapper.class, DataEditor.class);
        getEditorMap().put(ExtraCategoryInterpolatorWrapper.class,
                DataEditor.class);
        getEditorMap().put(CaseExpanderWrapper.class, DataEditor.class);
//        getEditorMap().put(CorrMatrixConverter.class, DataEditor.class);

        getEditorMap().put(BootstrapSamplerWrapper.class, DataEditor.class);
        getEditorMap().put(BootstrapSamplerParams.class,
                BootstrapSamplerParamsEditor.class);
        getEditorMap().put(MissingDataInjectorWrapper.class, DataEditor.class);
        getEditorMap().put(MissingDataInjectorParams.class,
                MissingDataInjectorParamsEditor.class);

        getEditorMap().put(BayesDataParams.class, BayesDataParamsEditor.class);
        getEditorMap().put(SemDataParams.class, SemDataParamsEditor.class);
        getEditorMap().put(MeasurementSimulatorParams.class,
                MeasurementSimulatorPropertyEditor.class);

        // Estimator
        getEditorMap().put(BayesEstimatorWrapper.class,
                BayesEstimatorEditor.class);
        getEditorMap().put(SemEstimatorWrapper.class, SemImEditor.class);
        getEditorMap().put(DirichletEstimatorWrapper.class,
                DirichletBayesImEditor.class);
        getEditorMap().put(EmBayesEstimatorWrapper.class,
                EmBayesEstimatorEditor.class);
        getEditorMap().put(StructEmBayesSearchRunner.class,
                StructEmBayesSearchEditor.class);

        // Updater
        getEditorMap().put(CptInvariantUpdaterWrapper.class,
                BayesUpdaterEditor.class);
        getEditorMap().put(RowSummingExactWrapper.class,
                BayesUpdaterEditor.class);
        getEditorMap().put(ApproximateUpdaterWrapper.class,
                BayesUpdaterEditor.class);
        getEditorMap().put(SemUpdaterWrapper.class, SemUpdaterEditor.class);

        // Classify
        getEditorMap().put(BayesUpdaterClassifierWrapper.class,
                BayesUpdaterClassifierEditor.class);

        // Search editors.
        getEditorMap().put(PcRunner.class, PcGesSearchEditor.class);
        getEditorMap().put(CpcRunner.class, PcGesSearchEditor.class);
        getEditorMap().put(LocalOrientationTestRunner.class,
                PcGesSearchEditor.class);
        getEditorMap().put(PcdRunner.class, PcGesSearchEditor.class);
        getEditorMap().put(FciRunner.class, FciCcdSearchEditor.class);
        getEditorMap().put(CfciRunner.class, FciCcdSearchEditor.class);
        getEditorMap().put(CcdRunner.class, FciCcdSearchEditor.class);
        getEditorMap().put(GesRunner.class, PcGesSearchEditor.class);

        getEditorMap().put(PurifyRunner.class, MimSearchEditor.class);
        getEditorMap().put(MimBuildRunner.class, MimSearchEditor.class);
        getEditorMap().put(BuildPureClustersRunner.class,
                MimSearchEditor.class);

//        getEditorMap().put(PcxRunner.class, MbSearchEditor.class);
//        getEditorMap().put(MbfsRunner.class, MbSearchEditor.class);
//        getEditorMap().put(CeFanSearchRunner.class, MbSearchEditor.class);

        // Regression
       getEditorMap().put(RegressionRunner.class, RegressionEditor.class);
       getEditorMap().put(LogisticRegressionRunner.class,
               LogisticRegressionEditor.class);

//        getEditorMap().put(GaRunner.class, GaSearchEditor.class);

        // Search param editors.
//        getEditorMap().put(BasicSearchParams.class, SearchParamEditor.class);
//        getEditorMap().put(PcSearchParams.class, PcSearchParamEditor.class);
//        getEditorMap().put(FciSearchParams.class, FciSearchParamEditor.class);
//        getEditorMap().put(FciSearchParams.class, FciSearchParamEditor.class);
//        getEditorMap().put(GaParams.class, SearchGaParamEditor.class);
//        getEditorMap().put(GesParams.class, SearchParamGesEditor.class);
//        getEditorMap().put(MbSearchParams.class, MbSearchParamEditor.class);
//        getEditorMap().put(PurifyParams.class, PurifyParamsEditor.class);
//        getEditorMap().put(BuildPureClustersParams.class,
//                BuildPureClustersParamsEditor.class);
//        getEditorMap().put(MimBuildParams.class, MimBuildParamsEditor.class);

        // Comparison
        getEditorMap().put(GraphComparison.class, DataEditor.class);
        getEditorMap().put(GraphComparisonParams.class,
                GraphComparisonParamsEditor.class);

        //Regression
   //    getEditorMap().put(RegressionParams.class,
   //            RegressionParamsEditor.class);
   //     getEditorMap().put(LogisticRegressionParams.class,
   //             LogisticRegressionParamsEditor.class);

        //Prediction
        getEditorMap().put(PredictionRunner.class, PredictionEditor.class);

        // Note.
        getEditorMap().put(NoteModel.class, NoteEditor.class);

        // Sanity check to make sure all editors implement JPanel. If they
        // don't, wierd things happen, like odd grey areas appearing when
        // frames are printed.
        for (Object o : getEditorMap().keySet()) {
            Class key = (Class) o;
            Class value = getEditorMap().get(key);

            if (!(JPanel.class.isAssignableFrom(value))) {
                System.out.println(
                        "PLEASE FIX IMMEDIATELY: This editor class " +
                                "(identified in SessionConfig) does not implement JPanel " +
                                "\nand will therefore not behave properly: " +
                                value + ".");
                System.exit(1);
            }
        }
    }

    /**
     * Initializes the map from model classes to their parameterizing model
     * classes. When an entry is put into this map, the session node stores an
     * object of the corresponding parameterizing model class for each type of
     * model it instantiates. Parameterizing model classes must have blank
     * constructors.
     */
    private void initializeParamsMap() {
        this.paramsMap = MultiKeyMap.decorate(new HashedMap());

        // Graph.
        getParamsMap().put(DagWrapper.class, null, GraphParams.class);
        getParamsMap().put(SemGraphWrapper.class, null, GraphParams.class);
        getParamsMap().put(GraphWrapper.class, null, GraphParams.class);
        getParamsMap().put(RandomActiveLagGraph.class, null,
                LagGraphParams.class);

        // Data.
        getParamsMap().put(DataWrapper.class, MlBayesIm.class,
                BayesDataParams.class);
        getParamsMap().put(DataWrapper.class, DirichletBayesIm.class,
                BayesDataParams.class);
        getParamsMap().put(DataWrapper.class, SemIm.class, SemDataParams.class);
        getParamsMap().put(GeneSimDataWrapper.class, null,
                MeasurementSimulatorParams.class);

        getParamsMap().put(BayesDataWrapper.class, null, BayesDataParams.class);
        getParamsMap().put(DirichletBayesDataWrapper.class, null,
                BayesDataParams.class);
        getParamsMap().put(SemDataWrapper.class, null, SemDataParams.class);

        // PM
        getParamsMap().put(BayesPmWrapper.class, null, BayesPmParams.class);

        // IM
        getParamsMap().put(BayesImWrapper.class, null, BayesImParams.class);
        getParamsMap().put(DirichletBayesImWrapper.class, null,
                DirichletBayesImParams.class);

        // Estimator
        getParamsMap().put(DirichletEstimatorWrapper.class, null,
                DirichletEstimatorParams.class);
        getParamsMap().put(EmBayesEstimatorWrapper.class, null,
                EmBayesEstimatorParams.class);

        // Updater.
        getParamsMap().put(RowSummingExactWrapper.class, null,
                UpdaterParams.class);
        getParamsMap().put(CptInvariantUpdaterWrapper.class, null,
                UpdaterParams.class);
        getParamsMap().put(ApproximateUpdaterWrapper.class, null,
                UpdaterParams.class);

        //Data
        getParamsMap().put(BootstrapSamplerWrapper.class, null,
                BootstrapSamplerParams.class);
        getParamsMap().put(MissingDataInjectorWrapper.class, null,
                MissingDataInjectorParams.class);

        // Search
        getParamsMap().put(PcRunner.class, null, PcSearchParams.class);
        getParamsMap().put(CpcRunner.class, null, PcSearchParams.class);
//        getParamsMap().put(LocalOrientationTestRunner.class, null,
//                PcSearchParams.class);
        getParamsMap().put(PcdRunner.class, null, PcSearchParams.class);
        getParamsMap().put(FciRunner.class, null, FciSearchParams.class);
        getParamsMap().put(CfciRunner.class, null, FciSearchParams.class);
        getParamsMap().put(CcdRunner.class, null, BasicSearchParams.class);
//        getParamsMap().put(PcxRunner.class, null, MbSearchParams.class);
        getParamsMap().put(MbfsRunner.class, null, MbSearchParams.class);
        getParamsMap().put(CeFanSearchRunner.class, null, MbSearchParams.class);
//        getParamsMap().put(GaRunner.class, GaParams.class);
        getParamsMap().put(GesRunner.class, null, GesParams.class);
        getParamsMap().put(MimBuildRunner.class, null, MimBuildParams.class);
        getParamsMap().put(BuildPureClustersRunner.class, null,
                BuildPureClustersParams.class);
        getParamsMap().put(PurifyRunner.class, null, PurifyParams.class);
        getParamsMap().put(StructEmBayesSearchRunner.class, null,
                StructEmBayesSearchParams.class);

        // Regression
        getParamsMap().put(RegressionRunner.class, null,
                RegressionParams.class);
        getParamsMap().put(LogisticRegressionRunner.class, null,
                LogisticRegressionParams.class);

        // Classifier.

        // Comparison.
        getParamsMap().put(GraphComparison.class, null,
                GraphComparisonParams.class);

    }

    private void initializeDescripMap() {
        descripMap = new HashMap<Class, String>();

        // Graph.
        getDescripMap().put(DagWrapper.class, "Directed Acyclic Graph");
        getDescripMap().put(SemGraphWrapper.class,
                "Structural Equation Model Graph");
        getDescripMap().put(GraphWrapper.class, "General Graph");
        getDescripMap().put(CompletedPatternWrapper.class, "Completed Pattern");
        //        getDescripMap().put(ManualActiveLagGraph.class, "Time Series Graph");
        getDescripMap().put(RandomActiveLagGraph.class, "Time Series Graph");

        // PM.
        getDescripMap().put(BayesPmWrapper.class, "Bayes Parametric Model");
        getDescripMap().put(SemPmWrapper.class, "SEM Parametric Model");
        getDescripMap().put(BooleanGlassGenePm.class, "Boolean Glass Gene PM");

        // IM.
        getDescripMap().put(BayesImWrapper.class, "Bayes Instantiated Model");
        getDescripMap().put(DirichletBayesImWrapper.class,
                "Dirichlet Bayes Instantiated Model");
        getDescripMap().put(SemImWrapper.class, "SEM Instantiated Model");
        getDescripMap().put(BooleanGlassGeneIm.class, "Boolean Glass Gene IM");

        //Estimator.
        getDescripMap().put(DirichletEstimatorWrapper.class,
                "Dirichlet Estimator");
        getDescripMap().put(BayesEstimatorWrapper.class, "ML Bayes Estimator");
        getDescripMap().put(SemEstimatorWrapper.class, "SEM Estimator");
        getDescripMap().put(EmBayesEstimatorWrapper.class,
                "EM Bayes Estimator");

        // Data
        getDescripMap().put(BayesDataWrapper.class, "Bayes IM Simulated Data");
        getDescripMap().put(VariableSubsetterWrapper.class,
                "Create a subset of selected columns.");
        getDescripMap().put(ModeInterpolatorWrapper.class,
                "Substitute column mode for missing values.");
        getDescripMap().put(BootstrapSamplerWrapper.class,
                "Sample new dataset with replacement.");
        getDescripMap().put(MissingDataInjectorWrapper.class,
                "Insert missing data according to specified prob.");
        getDescripMap().put(ExtraCategoryInterpolatorWrapper.class,
                "Use \"MissingValue\" category for missing values.");
        getDescripMap().put(CaseExpanderWrapper.class,
                "Repeat cases that have case multipliers.");
//        getDescripMap().put(CorrMatrixConverter.class,
//                "Convert continuous data set to correlation matrix.");
        getDescripMap().put(DirichletBayesDataWrapper.class,
                "Dirichlet Bayes IM Simulated Data");
        getDescripMap().put(SemDataWrapper.class, "Sem IM Simulated Data");
        getDescripMap().put(GeneSimDataWrapper.class,
                "Boolean " + "Glass Gene IM Simulated Data");
        getDescripMap().put(DataWrapper.class, "Data Set List");

        // Search.
        getDescripMap().put(PcRunner.class, "PC");
        getDescripMap().put(CpcRunner.class, "CPC");
//        getDescripMap().put(LocalOrientationTestRunner.class,
//                "Local Orientation");
        getDescripMap().put(PcdRunner.class, "PCD");
        getDescripMap().put(FciRunner.class, "FCI");
        getDescripMap().put(CfciRunner.class, "CFCI");
        getDescripMap().put(CcdRunner.class, "CCD");
//        getDescripMap().put(PcxRunner.class, "PCX");
        getDescripMap().put(MbfsRunner.class, "MBFS");
        getDescripMap().put(CeFanSearchRunner.class, "CEFS");
        getDescripMap().put(GesRunner.class, "GES");
        getDescripMap().put(MimBuildRunner.class, "MIMBuild");
        getDescripMap().put(BuildPureClustersRunner.class, "BPC");
        getDescripMap().put(PurifyRunner.class, "Purify");
        getDescripMap().put(StructEmBayesSearchRunner.class,
                "Structural EM Bayes");

        // Regression.
        getDescripMap().put(RegressionRunner.class,
                "Multiple Linear Regression");
        getDescripMap().put(LogisticRegressionRunner.class,
                "Logistic Regression");

        // Updater.
        getDescripMap().put(RowSummingExactWrapper.class,
                "Row Summing Exact Updater");
        getDescripMap().put(CptInvariantUpdaterWrapper.class,
                "CPT Invariant Exact Updater");
        getDescripMap().put(ApproximateUpdaterWrapper.class,
                "Approximate Updater");
        getDescripMap().put(SemUpdaterWrapper.class, "SEM Updater");

        // Classifier.
        getDescripMap().put(BayesUpdaterClassifierWrapper.class,
                "Bayes Updater Classifier");

        // Comparison.
        getDescripMap().put(GraphComparison.class, "Graph Comparison");

        //Prediction
        getDescripMap().put(PredictionRunner.class, "Prediction");

        // Note
        getDescripMap().put(NoteModel.class, "Note");
    }

    private void initializeAcronymMap() {
        this.acronymMap = new HashMap<Class, String>();

        // Graph
        getAcronymMap().put(DagWrapper.class, "DAG");
        getAcronymMap().put(SemGraphWrapper.class, "SEM Graph");
        getAcronymMap().put(GraphWrapper.class, "Graph");
        getAcronymMap().put(CompletedPatternWrapper.class, "Completed Pattern");
        getAcronymMap().put(ManualActiveLagGraph.class, "New LG");
        getAcronymMap().put(RandomActiveLagGraph.class, "New LG");

        // PM
        getAcronymMap().put(BayesPmWrapper.class, "Bayes PM");
        getAcronymMap().put(SemPmWrapper.class, "SEM PM");
        getAcronymMap().put(BooleanGlassGenePm.class, "Glass PM");

        // IM
        getAcronymMap().put(BayesImWrapper.class, "Bayes IM");
        getAcronymMap().put(DirichletBayesImWrapper.class, "Dir Bayes IM");
        getAcronymMap().put(SemImWrapper.class, "SEM IM");
        getAcronymMap().put(BooleanGlassGeneIm.class, "Glass IM");

        // Data
        getAcronymMap().put(BayesDataWrapper.class, "Bayes Data");
        getAcronymMap().put(SemDataWrapper.class, "SEM Data");
        getAcronymMap().put(DirichletBayesDataWrapper.class, "Dir Bayes Data");
        getAcronymMap().put(GeneSimDataWrapper.class, "Gene Data");
        getAcronymMap().put(DataWrapper.class, "Data");

        // Data Manipulation
        getAcronymMap().put(VariableSubsetterWrapper.class, "Variable Subset");
        getAcronymMap().put(ModeInterpolatorWrapper.class, "Mode Int");
        getAcronymMap().put(ExtraCategoryInterpolatorWrapper.class,
                "ExtraVal Int");
        getAcronymMap().put(CaseExpanderWrapper.class, "CaseExp");
//        getAcronymMap().put(CorrMatrixConverter.class, "CorrMatrix");
        getAcronymMap().put(BootstrapSamplerWrapper.class, "Bootstrap");
        getAcronymMap().put(MissingDataInjectorWrapper.class,
                "Insert miss data");

        // Estimator
        getAcronymMap().put(BayesEstimatorWrapper.class, "ML Bayes Est");
        getAcronymMap().put(SemEstimatorWrapper.class, "SEM Est");
        getAcronymMap().put(DirichletEstimatorWrapper.class, "Dir Bayes Est");
        getAcronymMap().put(EmBayesEstimatorWrapper.class, "EM Bayes Est");

        // Search
        getAcronymMap().put(PcRunner.class, "PC");
        getAcronymMap().put(CpcRunner.class, "CPC");
//        getAcronymMap().put(LocalOrientationTestRunner.class, "LO");
        getAcronymMap().put(PcdRunner.class, "PCD");
        getAcronymMap().put(FciRunner.class, "FCI");
        getAcronymMap().put(CfciRunner.class, "CFCI");
        getAcronymMap().put(CcdRunner.class, "CCD");
//        getAcronymMap().put(PcxRunner.class, "PCX");
        getAcronymMap().put(MbfsRunner.class, "MBF");
        getAcronymMap().put(CeFanSearchRunner.class, "CEF");
//        getAcronymMap().put(GaRunner.class, "GA");
        getAcronymMap().put(GesRunner.class, "GES");
        getAcronymMap().put(MimBuildRunner.class, "MIMBuild");
        getAcronymMap().put(BuildPureClustersRunner.class, "BPC");
        getAcronymMap().put(PurifyRunner.class, "Purify");
        getAcronymMap().put(PurifyRunner.class, "Purify");
        getAcronymMap().put(StructEmBayesSearchRunner.class, "Struct EM");

        // Regression
        getAcronymMap().put(RegressionRunner.class, "Mult Lin Reg");
        getAcronymMap().put(LogisticRegressionRunner.class, "Log Reg");

        // Updater
        getAcronymMap().put(RowSummingExactWrapper.class, "Row Sum");
        getAcronymMap().put(CptInvariantUpdaterWrapper.class, "CPT Inv");
        getAcronymMap().put(ApproximateUpdaterWrapper.class, "Approx Up");
        getAcronymMap().put(SemUpdaterWrapper.class, "SEM Up");

        // Comparison
        getAcronymMap().put(GraphComparison.class, "Graph Comp");

        // Classifier
        getAcronymMap().put(BayesUpdaterClassifierWrapper.class,
                "BayesUpClass");

        // Prediction
        getAcronymMap().put(PredictionRunner.class, "Predict");

        // Note
        getAcronymMap().put(NoteModel.class, "Note");
    }

    /**
     * Builds a map from classes to the short names used to refer to them in the
     * help system.
     */
    private void initializeHelpNameMap() {
        helpNameMap = new HashMap<Class, String>();

        // Graph.
        getHelpNameMap().put(DagWrapper.class, "dag");
        getHelpNameMap().put(SemGraphWrapper.class, "sem_graph");
        getHelpNameMap().put(GraphWrapper.class, "general_graph");
        getHelpNameMap().put(CompletedPatternWrapper.class,
                "completed_pattern");
        getHelpNameMap().put(RandomActiveLagGraph.class, "time_graph");

        // PM.
        getHelpNameMap().put(BayesPmWrapper.class, "bayes_pm");
        getHelpNameMap().put(SemPmWrapper.class, "sem_pm");
        getHelpNameMap().put(BooleanGlassGenePm.class, "glass_pm");

        // IM.
        getHelpNameMap().put(BayesImWrapper.class, "bayes_im");
        getHelpNameMap().put(DirichletBayesImWrapper.class,
                "dirichlet_bayes_im");
        getHelpNameMap().put(SemImWrapper.class, "sem_im");
        getHelpNameMap().put(BooleanGlassGeneIm.class, "glass_im");

        //Estimator.
        getHelpNameMap().put(DirichletEstimatorWrapper.class,
                "dirichlet_estimator");
        getHelpNameMap().put(BayesEstimatorWrapper.class, "bayes_estimator");
        getHelpNameMap().put(SemEstimatorWrapper.class, "sem_estimator");
        getHelpNameMap().put(EmBayesEstimatorWrapper.class,
                "em_bayes_estimator");

        // Data
        getHelpNameMap().put(BayesDataWrapper.class, "data");
        getHelpNameMap().put(DirichletBayesDataWrapper.class, "data");
        getHelpNameMap().put(SemDataWrapper.class, "data");
        getHelpNameMap().put(GeneSimDataWrapper.class, "data");
        getHelpNameMap().put(DataWrapper.class, "data");

        // Data manipulation.
        getHelpNameMap().put(VariableSubsetterWrapper.class, "column_subset");
        getHelpNameMap().put(ModeInterpolatorWrapper.class,
                "mode_interpolator");
        getHelpNameMap().put(BootstrapSamplerWrapper.class,
                "bootstrap_sampler");
        getHelpNameMap().put(MissingDataInjectorWrapper.class,
                "missing_data_injector");
        getHelpNameMap().put(ExtraCategoryInterpolatorWrapper.class,
                "extra_category_interpolator");
        getHelpNameMap().put(CaseExpanderWrapper.class, "case_expander");
//        getHelpNameMap().put(CorrMatrixConverter.class,
//                "correlation_matrix_converter");

        // Search.
        getHelpNameMap().put(PcRunner.class, "pc");
        getHelpNameMap().put(CpcRunner.class, "cpc");
        getHelpNameMap().put(PcdRunner.class, "pcd");
        getHelpNameMap().put(FciRunner.class, "fci");
        getHelpNameMap().put(CfciRunner.class, "cfci");
        getHelpNameMap().put(CcdRunner.class, "ccd");
        getHelpNameMap().put(GesRunner.class, "ges");
        getHelpNameMap().put(MbfsRunner.class, "mbf");
        getHelpNameMap().put(CeFanSearchRunner.class, "cef");
        getHelpNameMap().put(MimBuildRunner.class, "mimbuild");
        getHelpNameMap().put(BuildPureClustersRunner.class, "bpc");
        getHelpNameMap().put(PurifyRunner.class, "purify");
//        getHelpNameMap().put(StructEmBayesSearchRunner.class, "structural_em");

        // Regression.
        getHelpNameMap().put(RegressionRunner.class,
                "Multiple Linear Regression");
        getHelpNameMap().put(LogisticRegressionRunner.class,
                "Logistic Regression");

        // Updater.
        getHelpNameMap().put(RowSummingExactWrapper.class,
                "row_summing_updater");
        getHelpNameMap().put(CptInvariantUpdaterWrapper.class, "cpt_updater");
        getHelpNameMap().put(ApproximateUpdaterWrapper.class,
                "approximate_updater");
        getHelpNameMap().put(SemUpdaterWrapper.class, "sem_updater");

        // Classifier.
        getHelpNameMap().put(BayesUpdaterClassifierWrapper.class,
                "bayes_updater_classifier");

        // Comparison.
        getHelpNameMap().put(GraphComparison.class, "graph_comparison");

        //Prediction
        getHelpNameMap().put(PredictionRunner.class, "prediction");

        // Note
        getHelpNameMap().put(NoteModel.class, "note");
    }

    private void initializeSessionNodeImagesMap() {
        this.sessionNodeImagesMap = new HashMap<String, String>();

        getSessionNodeImagesMap().put("Graph", "graphIcon.gif");
        getSessionNodeImagesMap().put("PM", "pmIcon.gif");
        getSessionNodeImagesMap().put("IM", "imIcon.gif");
        getSessionNodeImagesMap().put("Data", "dataIcon.gif");
        getSessionNodeImagesMap().put("ManipData", "dataIcon.gif");
        getSessionNodeImagesMap().put("Estimator", "estimatorIcon.gif");
        getSessionNodeImagesMap().put("Updater", "searchIcon.gif");
        getSessionNodeImagesMap().put("MB", "searchIcon.gif");
        getSessionNodeImagesMap().put("Classify", "searchIcon.gif");
        getSessionNodeImagesMap().put("Search", "searchIcon.gif");
        getSessionNodeImagesMap().put("Regression", "searchIcon.gif");
        getSessionNodeImagesMap().put("Compare", "graphIcon.gif");
        getSessionNodeImagesMap().put("Predict", "graphIcon.gif");
        getSessionNodeImagesMap().put("Note", "note.png");
    }

    private void initializeSessionNodeTooltipsMap() {
        this.sessionNodeTooltipsMap = new HashMap<String, String>();

        getSessionNodeTooltipsMap().put("Graph", "<html>" +
                "Allowable Input Nodes:" + "<br>(1) No inputs, OR" +
                "<br>(2) Data (to extract variables)" +
                "<br><i>Also (to extract a graph) any of these:" +
                "<br>Search, PM, IM, Estimator, Updater</i>" + "</html>");
        getSessionNodeTooltipsMap().put("PM", "<html>" +
                "Allowable Input Nodes:" + "<br>(1) Graph, OR" +
                "<br>(2) Graph AND Data (to extract discrete categories)" +
                "<br><i>Also (to extract a PM) any of these:" +
                "<br>IM, Estimator</i>" + "</html>");
        getSessionNodeTooltipsMap().put("IM", "<html>" +
                "Allowable Input Nodes:" + "<br>(1) PM (Bayes or SEM), OR" +
                "<br>(2) IM (to convert Dirichlet Bayes IM to Bayes IM), OR" +
                "<br>(3) Estimator (to extract IM)" + "</html>");
        getSessionNodeTooltipsMap().put("Data", "<html>" +
                "Allowable Input Nodes:" +
                "<br>(1) No inputs (standalone data set), OR" +
                "<br>(2) IM (Bayes or SEM, to simulate data)" + "</html>");
        getSessionNodeTooltipsMap().put("ManipData", "<html>" +
                "Allowable Input Node:" + "<br>(1) Data" + "</html>");
        getSessionNodeTooltipsMap().put("Estimator", "<html>" +
                "Allowable Input Combinations:" + "<br>(1) PM AND Data, OR" +
                "<br>(1) IM AND Data (shortcut for IM-->PM + Data)" +
                "</html>");
        getSessionNodeTooltipsMap().put("Updater", "<html>" +
                "Allowable Input Nodes:" + "<br>(1) IM (Bayes only)" +
                "</html>");
        getSessionNodeTooltipsMap().put("Classify", "<html>" +
                "Allowable Input Combination:" +
                "<br>(1) IM (Bayes only) + Data (discrete only)" + "</html>");
        getSessionNodeTooltipsMap().put("Search", "<html>" +
                "Allowable Input Nodes:" +
                "<br>(1) Data (Continuous or Discrete), OR" +
                "<br>(2) Graph (searches directly over graph) OR" +
                "<br>(3) for struc EM: Discrete data + Bayes PM" + "</html>");
        getSessionNodeTooltipsMap().put("Regression", "<html>" +
                "Allowable Input Combination:" + "<br>(1) Data (Continuous)" +
                "</html>");
        getSessionNodeTooltipsMap().put("Compare", "<html>" +
                "Allowable Input Combination:" + "<br>(1) Search AND Graph" +
                "<br>(2) Graph (reference) AND Graph (target)" + "</html>");
        getSessionNodeTooltipsMap().put("Predict", "<html>" +
                "Allowable Input Combination:" +
                "<br>Search (FCI) AND Data (continuous)" + "</html>");
        getSessionNodeTooltipsMap().put("Predict", "<html>" +
                "Place a note in the session. No parents." +
                "</html>");

    }

    private void initializeModelChooserTitlesMap() {
        this.modelChooserTitlesMap = new HashMap<String, String>();

        getModelChooserTitlesMap().put("Graph", "Graph Types");
        getModelChooserTitlesMap().put("PM", "Types of Parameterized Models");
        getModelChooserTitlesMap().put("IM", "Types of Instantiated Models");
        getModelChooserTitlesMap().put("Data", "Types of Data Objects");
        getModelChooserTitlesMap().put("ManipData",
                "Types of Data Manipulations");
        getModelChooserTitlesMap().put("Estimator", "Types of Estimators");
        getModelChooserTitlesMap().put("Updater", "Types of Updaters");
        getModelChooserTitlesMap().put("Classify", "Types of Classifiers");
        getModelChooserTitlesMap().put("Search", "Types of Search Algorithms");
        getModelChooserTitlesMap().put("Compare", "Types of Comparisons");
        getModelChooserTitlesMap().put("Predict", "Types of Predictions");
        getModelChooserTitlesMap().put("Note", "Types of Notes");
    }

    public Map<String, Class[]> getModelClassesMap() {
        return this.modelClassesMap;
    }

    public Map<Class, Class> getEditorMap() {
        return this.editorMap;
    }

    public MultiKeyMap getParamsMap() {
        return this.paramsMap;
    }

    public Map<Class, String> getDescripMap() {
        return this.descripMap;
    }

    public Map<Class, String> getAcronymMap() {
        return this.acronymMap;
    }

    public Map<Class, String> getHelpNameMap() {
        return this.helpNameMap;
    }

    public Map<String, String> getSessionNodeImagesMap() {
        return this.sessionNodeImagesMap;
    }

    public Map<String, String> getSessionNodeTooltipsMap() {
        return this.sessionNodeTooltipsMap;
    }

    public Map<String, String> getModelChooserTitlesMap() {
        return modelChooserTitlesMap;
    }
}


