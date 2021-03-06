/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.v2_3.planner.logical.steps

import org.neo4j.cypher.internal.compiler.v2_3.ast
import org.neo4j.cypher.internal.compiler.v2_3.ast.Identifier
import org.neo4j.cypher.internal.compiler.v2_3.pipes.{Ascending, Descending, SortDescription}
import org.neo4j.cypher.internal.compiler.v2_3.planner.logical._
import org.neo4j.cypher.internal.compiler.v2_3.planner.logical.plans._
import org.neo4j.cypher.internal.compiler.v2_3.planner.{PlannerQuery, QueryProjection}

object sortSkipAndLimit extends PlanTransformer[PlannerQuery] {

  import org.neo4j.cypher.internal.compiler.v2_3.planner.logical.steps.LogicalPlanProducer._

  def apply(plan: LogicalPlan, query: PlannerQuery)(implicit context: LogicalPlanningContext): LogicalPlan = query.horizon match {
    case p: QueryProjection =>
      val shuffle = p.shuffle
      val producedPlan = (shuffle.sortItems.toList, shuffle.skip, shuffle.limit) match {
        case (Nil, s, l) =>
          addLimit(l, addSkip(s, plan))

        case (sortItems, None, Some(l)) =>
          planSortedLimit(plan, l, sortItems)

        case (sortItems, Some(s), Some(l)) =>
          planSortedSkipAndLimit(plan, s, l, sortItems)

        case (sortItems, s, None) =>
          require(sortItems.forall(_.expression.isInstanceOf[Identifier]))
          val sortDescriptions = sortItems.map(sortDescription)
          val sortPlan = planSort(plan, sortDescriptions, sortItems)
          addSkip(s, sortPlan)
      }

      producedPlan

    case _ => plan
  }

  private def sortDescription(in: ast.SortItem): SortDescription = in match {
    case ast.AscSortItem(ast.Identifier(key)) => Ascending(key)
    case ast.DescSortItem(ast.Identifier(key)) => Descending(key)
    case _ => ???
  }

  private def addSkip(s: Option[ast.Expression], plan: LogicalPlan): LogicalPlan =
    s.fold(plan)(x => planSkip(plan, x))

  private def addLimit(s: Option[ast.Expression], plan: LogicalPlan): LogicalPlan =
    s.fold(plan)(x => planLimit(plan, x))
}
