from flask import Flask, request, jsonify, json
import pymongo

app = Flask(__name__)
pokedex = [{'number': 14, 'name': 'Kakuna'},
           {'number': 16, 'name': 'Pidgey'},
           {'number': 50, 'name': 'Diglett'}]

# @app.route('/api/v1/pokemon', methods=('GET', 'POST'))
# def pokemon():
#     if request.method == 'GET':
#         response = pokedex
#     else:  # POST
#         if request.content_type != 'application/json':
#             return jsonify({})
#         try:
#             new_pokemon = json.loads(request.data)
#         except ValueError:
#             return jsonify({})
#
#         if 'number' in new_pokemon and isinstance(new_pokemon['number'], int) and \
#            'name' in new_pokemon and isinstance(new_pokemon['name'], basestring) and \
#            len(new_pokemon.keys()) == 2 and new_pokemon not in pokedex:
#             pokedex.append(new_pokemon)
#             response = pokedex[-1]
#         else:
#             return jsonify({})
#
#     return request.data
@app.route('/api/v1/pokemon', methods=('GET', 'POST'))
def pokemon():
    # if request.method == 'GET':
    #     response = pokedex
    # else:  # POST
    #     if request.content_type != 'application/json':
    #         return jsonify({})
    #     try:
    #         new_pokemon = json.loads(request.data)
    #     except ValueError:
    #         return jsonify({})
    #
    #     if 'number' in new_pokemon and isinstance(new_pokemon['number'], int) and \
    #        'name' in new_pokemon and isinstance(new_pokemon['name'], basestring) and \
    #        len(new_pokemon.keys()) == 2 and new_pokemon not in pokedex:
    #         pokedex.append(new_pokemon)
    #         response = pokedex[-1]
    #     else:
    #         return jsonify({})

    # print(type(request.form))
    # print(request.form.keys())
    if request.method == 'POST':
        print(request.form["username"])
        print(type(request.form))
        print(request.form.to_dict())
        # print(json.dumps(request.data))

        # print(jsonify(str(request.data.decode('utf-8'))).replace("\"",""))
        return request.form["username"]
    return "aneesh u dumbass"

if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=True)
