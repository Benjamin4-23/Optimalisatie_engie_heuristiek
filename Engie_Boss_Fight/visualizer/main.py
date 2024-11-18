import matplotlib.pyplot as plt
import json
from enum import Enum, StrEnum
import pathlib


DOT_SIZE = 4
LINE_WIDTH = 2.5

class EDGE_TYPE(StrEnum):
    REGULAR = "regular"
    OFFSTREET = "offstreet"
    EXISTING = "existing"

class NODE_TYPE(StrEnum):
    REGULAR = "regular"
    PROSPECT = "prospect"


nodeMap = {}
nodeVisistMap = {}

class Node:
    def __init__(self, json_data):
        self.id = json_data["id"]
        self.x = json_data["coords"][0]
        self.y = json_data["coords"][1]
        self.type: NODE_TYPE = json_data["node_type"]

        nodeMap[self.id] = self
        nodeVisistMap[self.id] = False

    def get_coords(self):
        return (self.x, self.y)


class Edge:
    def __init__(self, json_data):
        self.type: EDGE_TYPE = json_data["edge_type"]
        self.src = json_data["endpoint1"]
        self.dest = json_data["endpoint2"]

    def get_connection(self):
        return (self.src, self.dest)


def read_dataset(filepath):
    with open(filepath, 'r') as f:
        data = json.load(f)
    return ([Node(node) for node in data["nodes"]],
            [Edge(edge) for edge in data["edges"]])

def read_output(filepath):
    with open(filepath, 'r') as f:
        data = json.load(f)["edges"]
    return data



def visualize(nodes, edges, connections):

    plt.figure(figsize=(20, 20))

    for edge in edges:
        if edge.type != EDGE_TYPE.EXISTING:
            continue

        srcID, destID = edge.get_connection()
        src_x, src_y = nodeMap[srcID].get_coords()
        dest_x, dest_y = nodeMap[destID].get_coords()
        if not nodeVisistMap[srcID]:
            plt.plot(src_x, src_y, 'ro', markersize=DOT_SIZE)
            nodeVisistMap[srcID] = True

        if not nodeVisistMap[destID]:
            plt.plot(dest_x, dest_y, 'ro', markersize=DOT_SIZE)
            nodeVisistMap[destID] = True

        plt.plot([src_x, dest_x], [src_y, dest_y], 'r-', )

    for edge in connections:
        src_x, src_y = edge["startNode"]["x"], edge["startNode"]["y"]
        dest_x, dest_y = edge["endNode"]["x"], edge["endNode"]["y"]
        plt.plot([src_x, dest_x], [src_y, dest_y], 'g-')

    for node in nodes:
        if not nodeVisistMap[node.id]:
            x, y = node.get_coords()

            if node.type == NODE_TYPE.REGULAR:
                plt.plot(x, y, 'bo', markersize=DOT_SIZE)
            elif node.type == NODE_TYPE.PROSPECT:
                plt.plot(x, y, "orange", marker='o', markersize=DOT_SIZE)

    plt.grid(False)
    plt.axis('off')
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    print("Current path:", pathlib.Path().resolve())
    nodes, edges = read_dataset("../data/bretigny_62p_1147n_1235e.json")
    real_edges = read_output("../output/graph_data.json")
    visualize(nodes, edges, real_edges)
