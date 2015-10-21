package weka

import weka.attributeSelection.{GreedyStepwise, CfsSubsetEval, AttributeSelection}
import weka.core.Instances


class FeaturesExtractor(val inputInstances: Instances) {

  def extract(className: String): Array[Int] = {

    val instances = new Instances(inputInstances)

    val classInd = instances.attribute(className).index()
    instances.setClassIndex(classInd)

    (0 until instances.numAttributes()).map(instances.attribute).filter(_.name().trim.startsWith("class")).filter(_.index() != classInd)
    .foreach(a => {
      val aid = instances.attribute(a.name()).index()
      instances.deleteAttributeAt(aid)
    })

    val attsel = new AttributeSelection();  // package weka.attributeSelection!
    val eval = new CfsSubsetEval()
    val search = new GreedyStepwise()
    //search.setSearchBackwards(true)
    attsel.setEvaluator(eval)
    attsel.setSearch(search)
    attsel.SelectAttributes(instances)
    // obtain the attribute indices that were selected
    val res = attsel.selectedAttributes()
    res.foreach(i => {
      assert(i == instances.classIndex() || i == inputInstances.attribute(i).name().drop(2).toInt)
    })

    res

  }

}
