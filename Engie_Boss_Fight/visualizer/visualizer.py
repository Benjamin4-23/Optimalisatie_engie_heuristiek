import json
import pathlib
import matplotlib.pyplot as plt
from matplotlib.widgets import CheckButtons
from enum import Enum

DOT_SIZE = 1.5
LINE_WIDTH = 2.5

nodeMap = {}
nodeVisistMap = {}
edgeConnMap = {}


class EDGE_TYPE(str, Enum):
    REGULAR = "regular"
    OFFSTREET = "offstreet"
    EXISTING = "existing"


class NODE_TYPE(str, Enum):
    REGULAR = "regular"
    PROSPECT = "prospect"


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
        self.id = json_data["id"]
        self.type: EDGE_TYPE = json_data["edge_type"]
        self.src = json_data["endpoint1"]
        self.dest = json_data["endpoint2"]

    def get_connection(self):
        return (self.src, self.dest)


class ConnectionPoint:
    def __init__(self, src_x, src_y, dest_x, dest_y):
        self.x = [src_x, dest_x]
        self.y = [src_y, dest_y]

    def get_connection(self):
        return (self.x, self.y)


class MAP_COLORS(str, Enum):
    GREEN = "#2ca02c"
    DARK_GREEN = "#1f7741"
    OLIVE = "#808000"
    MAGENTA = "#ff00ff"
    PURPLE = "#9467bd"
    PINK = "#e377c2"
    BROWN = "#8c564b"
    CYAN = "#17becf"
    INDIGO = "#4b0082"


class Map:
    __layers = {}
    __layer_names = []
    __layer_info = {}
    __layer_visibility = {}
    __layer_drawings = {}
    __layer_drawings_prec = {}

    def __init__(self, dataset, debug=False):
        self.debug = debug
        self.nodes, self.edges = self._read_dataset(dataset)
        for edge in self.edges:
            srcID, destID = edge.get_connection()
            src_x, src_y = nodeMap[srcID].get_coords()
            dest_x, dest_y = nodeMap[destID].get_coords()
            edgeConnMap[edge.id] = (src_x, src_y, dest_x, dest_y)

    def _read_dataset(self, filepath):
        with open(filepath, 'r') as f:
            data = json.load(f)
        return ([Node(node) for node in data["nodes"]],
                [Edge(edge) for edge in data["edges"]])

    def add_layer(self, name: str, color: MAP_COLORS, data: list[ConnectionPoint], zorder=None):
        self.__layers[name] = data
        self.__layer_info[name] = {"color": color, "z": zorder}
        self.__layer_visibility[name] = True if zorder == 0 else False
        self.__layer_drawings_prec[name] = False
        self.__layer_drawings[name] = []
        self.__layer_names.append(name)

    def visualize(self):
        fig, ax = plt.subplots(figsize=(20, 20))
        rax = plt.axes([0.9, 0.9, 0.2, 0.1])
        r = []
        for label in self.__layer_names:
            r.append(self.__layer_visibility[label])
        check = CheckButtons(rax, self.__layer_names, r)

        for edge in self.edges:
            src_x, src_y, dest_x, dest_y = edgeConnMap[edge.id]
            ax.plot([src_x, dest_x], [src_y, dest_y], color='lightgrey', linestyle='-')
            if edge.type != EDGE_TYPE.EXISTING:
                continue

            srcID, destID = edge.get_connection()

            if not nodeVisistMap[srcID]:
                ax.plot(src_x, src_y, 'ro', markersize=DOT_SIZE, zorder=100)
                nodeVisistMap[srcID] = True

            if not nodeVisistMap[destID]:
                ax.plot(dest_x, dest_y, 'ro', markersize=DOT_SIZE, zorder=100)
                nodeVisistMap[destID] = True

            ax.plot([src_x, dest_x], [src_y, dest_y], 'r-')

        if self.debug:
            x_lim = ax.get_xlim()
            y_lim = ax.get_ylim()

            x_margin = (x_lim[1] - x_lim[0]) * 0.0005
            y_margin = (y_lim[1] - y_lim[0]) * 0.0005

        for node in self.nodes:
            x, y = node.get_coords()
            if not nodeVisistMap[node.id]:
                if node.type == NODE_TYPE.REGULAR:
                    ax.plot(x, y, 'bo', markersize=DOT_SIZE, zorder=100)
                elif node.type == NODE_TYPE.PROSPECT:
                    ax.plot(x, y, "orange", marker='o', markersize=DOT_SIZE, zorder=100)

            if self.debug:
                ax.text(x+x_margin, y+y_margin, node.id, fontsize=5, color='black', ha='left', va='top')


        for label in self.__layer_names:
            if self.__layer_visibility[label]:
                for connection in self.__layers[label]:
                    x, y = connection.get_connection()
                    line = ax.plot(x, y, color=self.__layer_info[label]["color"], linestyle='-')
                    self.__layer_drawings[label].append(line)
                self.__layer_drawings_prec[label] = True

        def toggle_visibility(label):
            visible = not self.__layer_visibility[label]
            self.__layer_visibility[label] = visible

            if self.__layer_drawings_prec[label]:
                for lines in self.__layer_drawings[label]:
                    for line in lines:
                        line.set_visible(visible)

            if not self.__layer_drawings_prec[label] and visible:
                for connection in self.__layers[label]:
                    x, y = connection.get_connection()
                    line = ax.plot(x, y, color=self.__layer_info[label]["color"], linestyle='-')
                    self.__layer_drawings[label].append(line)
                self.__layer_drawings_prec[label] = True

            plt.draw()

        check.on_clicked(toggle_visibility)
        fig.canvas.mpl_connect('scroll_event', self.__zoom)

        plt.grid(False)
        ax.axis('off')
        plt.tight_layout()
        plt.show()

    def __zoom(self, event):
        base_scale = 1.1
        ax = event.inaxes
        if event.button == 'up':
            scale_factor = 1 / base_scale
        elif event.button == 'down':
            scale_factor = base_scale
        else:
            return

        xlim = ax.get_xlim()
        ylim = ax.get_ylim()

        xdata = event.xdata
        ydata = event.ydata

        new_xlim = [xdata - (xdata - xlim[0]) * scale_factor,
                    xdata + (xlim[1] - xdata) * scale_factor]
        new_ylim = [ydata - (ydata - ylim[0]) * scale_factor,
                    ydata + (ylim[1] - ydata) * scale_factor]

        ax.set_xlim(new_xlim)
        ax.set_ylim(new_ylim)
        plt.draw()


def read_output(filepath) -> list[ConnectionPoint]:
    with open(filepath, 'r') as f:
        data = json.load(f)["edges"]
    return [ConnectionPoint(*edgeConnMap[edge]) for edge in data]

def read_node_output(filepath) -> list[ConnectionPoint]:
    with open(filepath, 'r') as f:
        data = json.load(f)["nodes"]
        connections = []
        for node in data:
            if len(node) < 2:
                raise "No list of node id's given!"

            if len(node) == 2:
                src_x, src_y = nodeMap[node[0]].get_coords()
                dest_x, dest_y = nodeMap[node[1]].get_coords()
                connections.append(ConnectionPoint(src_x, src_y, dest_x, dest_y))
            elif len(node) > 2:
                for i in range(len(node)-1):
                    src_x, src_y = nodeMap[node[i]].get_coords()
                    dest_x, dest_y = nodeMap[node[i+1]].get_coords()
                    connections.append(ConnectionPoint(src_x, src_y, dest_x, dest_y))
    return connections


if __name__ == "__main__":
    file = "bretigny_62p_1147n_1235e.json"
    map = Map("../data/" + file, debug=False)
    real_edges = read_output("../output/output_" + file)
    node_edges = read_node_output("../output/output_" + file)
    node_dijkstra_edges = read_node_output("../output/output_bretigny_62p_1147n_1235e_dijkstra.json")
    #node_shave_edges = read_node_output("../output/edges_after_shave.json")
    #map.add_layer("Real", MAP_COLORS.GREEN, real_edges, 0)
    #map.add_layer("Simplified map", MAP_COLORS.BROWN, node_shave_edges, 0)
    map.add_layer("Solution", MAP_COLORS.CYAN, node_dijkstra_edges, 1)
    map.add_layer("Simplified Solution", MAP_COLORS.PINK, node_edges, 2)
    map.visualize()
