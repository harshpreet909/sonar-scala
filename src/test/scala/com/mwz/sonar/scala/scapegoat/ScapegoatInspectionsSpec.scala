/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala.scapegoat

import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.sonar.api.batch.rule.Severity

/** Tests the generated scapegoat inspections file */
class ScapegoatInspectionsSpec extends AnyFlatSpec with Inspectors with Matchers {
  "The Scapegoat Inspections object" should "define all scapegoat inspections" in {
    ScapegoatInspections.AllInspections should have size 118
    ScapegoatInspections.AllInspections.distinct should have size 118
  }

  it should "not define the blacklisted scapegoat inspections" in {
    ScapegoatInspections.AllInspections.map(inspection => inspection.id) should contain noneOf (
      "com.sksamuel.scapegoat.inspections.collections.FilterDotSizeComparison",
      "com.sksamuel.scapegoat.inspections.collections.ListTail"
    )
  }

  it should "have all inspections with non-empty properties" in {
    forEvery(ScapegoatInspections.AllInspections) { inspection =>
      inspection.id should not be empty
      inspection.name should not be empty
    }
  }

  it should "have all inspections' ids start with com.sksamuel.scapegoat.inspections" in {
    forEvery(ScapegoatInspections.AllInspections) { inspection =>
      inspection.id should startWith("com.sksamuel.scapegoat.inspections.")
    }
  }

  it should "correctly define the ArrayEquals inspection" in {
    val arrayEquals = ScapegoatInspection(
      id = "com.sksamuel.scapegoat.inspections.collections.ArrayEquals",
      name = "Array equals",
      description = Some(
        "Array equals is not an equality check. Use a.deep == b.deep or convert to another collection type"
      ),
      defaultLevel = Level.Info
    )

    ScapegoatInspections.AllInspections should contain(arrayEquals)
  }

  "The Scapegoat Inspection Levels" should "correctly map to SonarQube severities" in {
    Level.Info.toRuleSeverity shouldBe Severity.INFO
    Level.Warning.toRuleSeverity shouldBe Severity.MINOR
    Level.Error.toRuleSeverity shouldBe Severity.MAJOR
  }
}
