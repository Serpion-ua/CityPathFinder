package model

class WeightedGraph(edges: Seq[WeightedEdge] = Seq.empty){
  private val edgesConnectedToNodeId =
    edges.map(edge => (edge.from.id, edge)).groupMap(_._1)(_._2).withDefaultValue(Seq.empty)

  private val reachableNodes = edges.map(_.to.id).toSet

  def edgesByNodeId(nodeId: NodeId): Seq[WeightedEdge] = edgesConnectedToNodeId(nodeId)

  def nodeIdHaveOutgoingEdges(nodeId: NodeId): Boolean = edgesConnectedToNodeId(nodeId).nonEmpty
  def nodeHaveIncomingEdges(nodeId: NodeId): Boolean = reachableNodes.contains(nodeId)
}
