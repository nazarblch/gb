package predictors

import db.DBUser

/**
 * Created by buzun on 03/10/15.
 */
trait Builder {

  def train(usersWithInterests: Vector[DBUser]): Predictor
}
