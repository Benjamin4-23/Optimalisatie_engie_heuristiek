import json
import pathlib
import matplotlib.pyplot as plt

DOT_SIZE = 4
LINE_WIDTH = 2.5

REGULAR = "regular"
OFFSTREET = "offstreet"
EXISTING = "existing"
PROSPECT = "prospect"


nodeMap = {}
nodeVisistMap = {}
edgeConnMap = {}

class Node:
    def __init__(self, json_data):
        self.id = json_data["id"]
        self.x = json_data["coords"][0]
        self.y = json_data["coords"][1]
        self.type = json_data["node_type"]

        nodeMap[self.id] = self
        nodeVisistMap[self.id] = False

    def get_coords(self):
        return (self.x, self.y)


class Edge:
    def __init__(self, json_data):
        self.id = json_data["id"]
        self.type = json_data["edge_type"]
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
        srcID, destID = edge.get_connection()
        src_x, src_y = nodeMap[srcID].get_coords()
        dest_x, dest_y = nodeMap[destID].get_coords()
        edgeConnMap[edge.id] = [[src_x, dest_x], [src_y, dest_y]]

        plt.plot([src_x, dest_x], [src_y, dest_y], color='lightgrey', linestyle='-')
        if edge.type != EXISTING:
            continue

        if not nodeVisistMap[srcID]:
            plt.plot(src_x, src_y, 'ro', markersize=DOT_SIZE)
            nodeVisistMap[srcID] = True

        if not nodeVisistMap[destID]:
            plt.plot(dest_x, dest_y, 'ro', markersize=DOT_SIZE)
            nodeVisistMap[destID] = True

        plt.plot([src_x, dest_x], [src_y, dest_y], 'r-')

    for edge in connections:
        arr = edgeConnMap[edge]
        plt.plot(arr[0], arr[1], 'g-')

    for node in nodes:
        if not nodeVisistMap[node.id]:
            x, y = node.get_coords()

            if node.type == REGULAR:
                plt.plot(x, y, 'bo', markersize=DOT_SIZE)
            elif node.type == PROSPECT:
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
