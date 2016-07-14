/*
  Copyright 2013-2015 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.wix.accord.transform

import MacroHelper._
trait MacroHelper[ C <: Context ] {
  /** The macro context (of type `C`), must be provided by the inheritor */
  protected val context: C

  import context.universe._

  def termName( symbol: String ): TermName = context.universe.newTermName( symbol )
  def resetAttrs( tree: Tree ): Tree = context.resetAllAttrs( tree )
  def rewriteExistentialTypes( tree: Tree ): Tree = tree

  def prettyPrint( tree: Tree ): String = {
    // Ouch. Taking a leaf from Li Haoyi, see:
    // https://github.com/lihaoyi/sourcecode/blob/master/sourcecode/shared/src/main/scala/sourcecode/SourceContext.scala
    val fileContent = new String( tree.pos.source.content )
    val start = tree.collect { case t => t.pos.start }.min
    val end = {
      // For some reason, ouv.pos.isRange doesn't work -- I'm probably missing something, but this is
      // a decent workaround for now.
      val rangeEnd = tree.collect { case t => t.pos.end }.max
      if ( start != rangeEnd )
        rangeEnd + 1
      else {
        // Point position, need to actually parse to figure out the slice size by parsing
        val g: scala.tools.nsc.Global =
        context.asInstanceOf[ reflect.macros.runtime.Context ].global   // TODO is this safe?
        val parser = g.newUnitParser( fileContent.substring( start ), "<Accord>" )
        parser.expr()
        start + parser.in.lastOffset
      }
    }

    fileContent.slice( start, end )
  }

  // Stubs for forwards compatibility with 2.11 APIs --
  final class compileTimeOnly(message: String) extends scala.annotation.StaticAnnotation
}

object MacroHelper {
  type Context = scala.reflect.macros.Context
}
