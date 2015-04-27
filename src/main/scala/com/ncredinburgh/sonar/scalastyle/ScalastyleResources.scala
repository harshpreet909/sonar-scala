/*
 * Sonar Scalastyle Plugin
 * Copyright (C) 2014 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.ncredinburgh.sonar.scalastyle

import java.io.InputStream
import com.typesafe.config.ConfigFactory
import org.scalastyle.ScalastyleError
import org.sonar.api.PropertyType
import scala.xml.{Elem, XML, Node}

/**
 * Provides access to the various .property and XML files that Scalastyle provides
 * to describe its checkers.
 */
object ScalastyleResources {

  private val definitions = xmlFromClassPath("/scalastyle_definition.xml")
  private val documentation = xmlFromClassPath("/scalastyle_documentation.xml")

  private val cfg = ConfigFactory.load(this.getClass.getClassLoader)

  def allDefinedRules: Seq[RepositoryRule] = for {
    checker <- definitions \\ "checker"
    clazz = (checker \ "@class").text
    id = (checker \ "@id").text
    desc = description(id)
    params = nodeToParams(checker, id)
  } yield RepositoryRule(clazz, id, desc, params)

  def nodeToParams(checker: Node, id: String): List[Param] = for {
    parameter <- (checker \\ "parameter").toList
    key = nodeToParameterKey(parameter)
    propertyType = nodeToPropertyType(parameter)
    description = nodeToPropertyDescription(parameter, id)
    defaultValue = nodeToDefaultValue(parameter)
  } yield Param(key, propertyType, description, defaultValue)

  def description(key: String): String = descriptionFromDocumentation(key) getOrElse cfg.getConfig(key).getString("description")

  def label(key: String): String = cfg.getConfig(key).getString("label")

  private def descriptionFromDocumentation(key: String): Option[String] = {
    documentation \\ "scalastyle-documentation" \ "check" find { _ \\ "@id" exists (_.text == key) } match {
      case Some(node) =>
        val description =  (node \ "justification").text.trim
        if (description != "") Some(description) else None
      case None => None
    }
  }

  private def nodeToParameterKey(n: Node): String = (n \ "@name").text.trim

  private def nodeToPropertyType(n: Node): PropertyType = (n \ "@type").text.trim match {
    case "string" => if ((n \ "@name").text == "regex") {
      PropertyType.REGULAR_EXPRESSION
    } else if ((n \ "@name").text == "header") {
      PropertyType.TEXT
    } else {
      PropertyType.STRING
    }
    case "integer" => PropertyType.INTEGER
    case "boolean" => PropertyType.BOOLEAN
    case _ => PropertyType.STRING
  }

  private def nodeToPropertyDescription(node: Node, id: String): String = {
    val key = nodeToParameterKey(node)
    description(s"$id.$key")
  }

  private def nodeToDefaultValue(n: Node): String = (n \ "@default").text.trim

  private def xmlFromClassPath(s: String): Elem =  XML.load(fromClassPath(s))

  private def fromClassPath(s: String): InputStream = classOf[ScalastyleError].getResourceAsStream(s)
}
