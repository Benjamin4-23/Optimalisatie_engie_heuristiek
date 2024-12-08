import os
import sys
import json

#########################
### Versie 18/11/2024 ###
#########################

class Edge:
    def __init__(self, id, endpoint1, endpoint2, cost, existing, offstreet):
        self.id = id
        self.endpoint1 = endpoint1
        self.endpoint2 = endpoint2
        self.cost = cost
        self.existing = existing
        self.offstreet = offstreet

class Graph:
    def __init__(self, filename=None):
        self.filename = filename
        self.prospects = []
        self.edges = {}
        self.adjecency_list = {}
        self.nodes = []

    def add_edge(self, id, from_node, to_node, weight, existing, offstreet):
        if from_node not in self.nodes:
            self.nodes.append(from_node)

        if to_node not in self.nodes:
            self.nodes.append(to_node)

        self.edges[id] = Edge(id, from_node, to_node, weight, existing, offstreet)

        if from_node not in self.adjecency_list:
            self.adjecency_list[from_node] = []
        self.adjecency_list[from_node].append(to_node)

        if to_node not in self.adjecency_list:
            self.adjecency_list[to_node] = []
        self.adjecency_list[to_node].append(from_node)

    def add_prospect(self, prospect_id):
        self.prospects.append(prospect_id)

def read_solution(filepath, input_edges, prospect_node_ids):
    g = Graph()

    f = open(filepath)
    data = json.load(f)
    cost = int(data['objective_value'])
    
    for i in data['edges']:
        if i not in input_edges:
            print("INFEASIBLE: Boog %d in de oplossing bestaat niet in de input" % i)
            return
        
        edge = input_edges[int(i)]
        g.add_edge(edge.id, edge.endpoint1, edge.endpoint2, edge.cost, edge.existing, edge.offstreet)
    f.close()

    for prospect_id in prospect_node_ids:
        g.add_prospect(prospect_id)

    return g, cost

def read_input(filepath):
    filepath = filepath.replace("\\", "/")

    input_edges = {}
    prospect_node_ids = []
    existing_node_ids = []

    f = open(filepath)
    data = json.load(f)

    for i in data['edges']:
        existing = True if i['edge_type'] == 'existing' else False
        offstreet = True if i['edge_type'] == 'offstreet' else False
        ep1 = int(i['endpoint1'])
        ep2 = int(i['endpoint2'])

        input_edges[int(i['id'])] = Edge(int(i['id']), ep1, ep2, int(i['cost']), existing, offstreet)

        if existing:
            if ep1 not in existing_node_ids:
                existing_node_ids.append(ep1)
            if ep2 not in existing_node_ids:
                existing_node_ids.append(ep2)

    for i in data['nodes']:        
        if i['node_type'] == 'prospect':
            prospect_node_ids.append(int(i['id']))

    f.close()
    return input_edges, existing_node_ids, prospect_node_ids

def has_path(graph, start, existing_node_ids, prospects):
    visited = set()
    stack = [start]
    while stack:
        node = stack.pop()
        if node in existing_node_ids:
            return True
        if node in visited:
            continue
        visited.add(node)
        stack.extend([n for n in graph.adjecency_list[node] if n not in visited and n not in prospects])

    return False

def validate(input_file, solution_file):
    input_edges, existing_node_ids, prospect_node_ids = read_input(input_file)
    solution_graph, reported_solution_cost = read_solution(solution_file, input_edges, prospect_node_ids)

    for prospect_id in prospect_node_ids:

        if prospect_id not in solution_graph.nodes:
            print("INFEASIBLE: Prospect %d zit niet in de oplossing" % prospect_id)
            return
        
        if len(solution_graph.adjecency_list[prospect_id]) > 1:
            print("INFEASIBLE: Prospect %d is verbonden met meerdere offstreet bogen" % prospect_id)
            return
        
        is_connected = has_path(solution_graph, prospect_id, existing_node_ids, solution_graph.prospects)
        if not is_connected:
            print("INFEASIBLE: Prospect %d is niet verbonden met het bestaande netwerk" % prospect_id)
            return 

    total_cost = 0
    for edge in solution_graph.edges.values():
        total_cost += edge.cost    
    print("De oplossing is feasible, totale kost: %d" % total_cost)

    if total_cost != reported_solution_cost:
        print("LET OP: Gerapporteerde kost (%d) komt niet overeen met de echte kost (%d)" % (reported_solution_cost, total_cost))
        return

if __name__ == "__main__":

    if len(sys.argv) != 3:
        #print("Gebruik: python validator.py <input_file> <solution_file>")
        #file = "bretigny_62p_1147n_1235e.json"
        #file = "bagnolet_353p_3844n_4221e.json"
        #file = "bretigny_576n_9591n_10353e.json"
        #file = "bagnolet_1366p_13523n_15065e.json"
        file = "bagnolet_2081p_18464n_20478e.json"
        validate(f"../data/{file}", f"../output/output_{file}")
    else:
        print("Oplossing in bestand %s wordt gevalideerd..." % sys.argv[2])
        validate(sys.argv[1], sys.argv[2])