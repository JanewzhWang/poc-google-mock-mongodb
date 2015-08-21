package initialize

import play.api.mvc.{Controller, Action}

class InitializeController extends Controller {
  
  def initialize = Action {
    try {
      MockInitializer.initializeProjects
      println("Initialize projects done!")
      MockInitializer.initializeRegions
      println("Initialize regions done!")
      MockInitializer.initializeZones
      println("Initialize zones done!")
      Ok("Initialize projects, regions, zones done!")
    } catch {
      case e: Throwable => BadRequest("initialize failed: " + e.getMessage)
    }
  }
  
}