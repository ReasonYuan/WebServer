package org.sgdtk;

/**
 * Adagrad-trained Linear model for classification
 *
 * Very simple and fast implementation of Adagrad.  Per-weight params handled by virtual method in base class
 * which are specialized here, and called on non-zero params (attested featurees).  This allows the call
 * not to have to wait around cycling a bunch of parameters that wont receive any updates.
 *
 * Note that L1 regularization is not performed as summarized succintly in Dyer's notes.  This technique proved
 * slow to update parameters.  Instead we continue to capitalize on Bottou's optimization for sparse vectors,
 * seeking a simple regularization parameter that can be applied per example.
 *
 * To make this work, L2 regularization is done using an average
 * of the actualized per-parameter learning rates across all features for each example (time t).
 * In practice this still does seem to work well, yielding much better generalization than non-regularized case,
 * while staying conservative enough not to undermine Adagrad while keeping the code simple and fast
 *
 * Having the training method coupled to the model is undesriable, this is caused by coupling in the updateWeights(),
 * which maybe doesnt belong here, but nothing here is persisted post training, so all the training details are
 * only required at train time.
 *
 * @author dpressel
 */
public class AdagradLinearModel extends LinearModel
{
    private ArrayDouble gg;

    // TODO: Be nice and allow an app. dev. to get at these!
    private static double EPS = 1e-8;

    private double sumEta;

    public AdagradLinearModel(int wlength)
    {
        super(wlength);
        gg = new ArrayDouble(wlength);
    }

    /**
     * Create empty but initialized model
     *
     * @param wlength The length of the feature vector
     * @param wdiv    scaling
     * @param wbias   bias
     */
    public AdagradLinearModel(int wlength, double wdiv, double wbias)
    {
        super(wlength, wdiv, wbias);
        gg = new ArrayDouble(wlength);

    }

    protected AdagradLinearModel(ArrayDouble weights, double wdiv, double wbias)
    {
        super(weights, wdiv, wbias);
        gg = new ArrayDouble(weights.size());
    }

    /**
     * Empty constructor
     */
    public AdagradLinearModel()
    {

    }

    // Force some sort of regularization, even if its small.  Trying to do the technique of tracking
    // ALPHA vector of the unnormalized sum at i for each place is too slow, as is trying to update non sparse values
    @Override
    protected void scaleWeights(double eta, double lambda)
    {

        if (sumEta != 0)
        {
            eta = sumEta / weights.size();
        }
        sumEta = 0.;
        super.scaleWeights(eta, lambda);
    }
    /**
     * Create ALPHA deep copy of this
     *
     * @return clone
     */
    @Override
    public Model prototype()
    {
        return new AdagradLinearModel(weights, wdiv, wbias);
    }

    @Override
    public double perWeightUpdate(int index, double grad, double eta)
    {
        gg.set(index, gg.get(index) + grad * grad);
        double etaThis = eta / Math.sqrt(gg.get(index) + EPS);
        sumEta += etaThis;
        return etaThis;
    }

}
