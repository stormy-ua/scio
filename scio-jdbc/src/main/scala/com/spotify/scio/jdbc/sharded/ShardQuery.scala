/*
 * Copyright 2020 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.scio.jdbc.sharded

sealed trait ShardQuery extends Serializable

final case class RangeShardQuery[T](range: Range[T], upperBoundInclusive: Boolean)
    extends ShardQuery
final case class PrefixShardQuery[T](prefix: T) extends ShardQuery

object ShardQuery {

  private val RangeQueryTemplate = "SELECT * FROM %s WHERE %s >= %s and %s %s %s"
  private val PrefixQueryTemplate = "SELECT * FROM %s WHERE %s LIKE '%s%%'"

  def toSelectStatement(shardQuery: ShardQuery, tableName: String, shardColumn: String): String =
    shardQuery match {
      case RangeShardQuery(range, upperBoundInclusive) =>
        val uppBoundOp = if (upperBoundInclusive) "<=" else "<"
        RangeQueryTemplate.format(
          tableName,
          shardColumn,
          range.lowerBound.toString,
          shardColumn,
          uppBoundOp,
          range.upperBound.toString
        )
      case PrefixShardQuery(prefix: String) =>
        PrefixQueryTemplate.format(tableName, shardColumn, prefix)
      case _ =>
        throw new UnsupportedOperationException("The shard query isn't supported")
    }

}
