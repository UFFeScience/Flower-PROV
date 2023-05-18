/**
 * @author Débora Pina
 * @email deborabpina@poli.ufrj.br
 */

//graph vai ser edges graph = result['graph'], edges = graph['edges'] edges[i] edges[i]['from']
 function bfs(edges, id_nodes, start, end){
    var queue = [];
    var visited = [];
    var parents = [];
    var current;

    visited[start] = true;
    queue.push(start);    

    while(queue.length){
        current = queue.shift();
        if (current === end){
            console.log("true");
            return true;
        }
        for (var i=0; i<Object.keys(edges).length; i++){
            for (var j=0; j<id_nodes.length; j++){
                if (j !== current && edges[i]['from']===current && edges[i]['to']===j && !visited[i]){
                    parents[i] = current;
                    visited[i] = true;
                    queue.push(j);
                }                
            }
        }
    }
    console.log("false");
    return false;
} 