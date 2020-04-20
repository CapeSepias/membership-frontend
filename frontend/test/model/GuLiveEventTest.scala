package model

import model.EventbriteTestObjects._
import com.gu.memsub.images.Grid.{GridResult}
import com.gu.memsub.images.GridDeserializer._
import model.RichEvent.{GuLiveEvent, GridImage}
import org.specs2.mock.Mockito
import play.api.test.PlaySpecification
import utils.Resource
import utils.Implicits._

class GuLiveEventTest extends PlaySpecification with Mockito {

  val event = eventWithName()
  val grid = Resource.getJson("model/grid/api-image.json")
  val gridResponse = grid.as[GridResult]

  "GuLiveEventTest" should {

    "contain metadata and socialUrl for an event image" in {

      val firstExport = gridResponse.data.exports.get(1)
      val image = GridImage(firstExport.assets, gridResponse.data.metadata, firstExport.master)
      val guEvent = GuLiveEvent(event, Some(image), None)

      guEvent.imgOpt.flatMap(_.metadata.flatMap(_.description)) mustEqual Some("It's Chris!")
      guEvent.imgOpt.flatMap(_.metadata.map(_.photographer)) mustEqual Some("Joe Bloggs/Guardian Images")

      guEvent.socialImgUrl.get mustEqual "http://some-media-thing/aede0da05506d0d8cb993558b7eb9ad1d2d3e675/0_130_1703_1022/500.jpg"
    }
  }
}
