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
package org.neo4j.cypher.internal.compiler.v2_3

import org.mockito.Mockito
import org.neo4j.cypher.internal.commons.CypherFunSuite
import org.neo4j.cypher.internal.compiler.v2_3.planDescription.InternalPlanDescription
import org.neo4j.graphdb.QueryExecutionType.QueryType

class ExplainExecutionResultTest extends CypherFunSuite {

  private val closer = mock[TaskCloser]
  private val result =
    new ExplainExecutionResult(closer, List.empty, mock[InternalPlanDescription], QueryType.READ_ONLY)

  test("should call taskCloser close on close") {
    result.close()

    Mockito.verify(closer, Mockito.times(1)).close(success = true)
  }

  test("should call taskCloser close on close from java wrapper") {
    result.javaIterator.close()

    Mockito.verify(closer, Mockito.times(1)).close(success = true)
  }

  test("should call taskCloser close on close from java columns wrapper") {
    result.javaColumnAs("something").close()

    Mockito.verify(closer, Mockito.times(1)).close(success = true)
  }

  override protected def beforeEach() { Mockito.reset(closer) }
}
