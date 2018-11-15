import flask
from flask import Flask, request, jsonify, g
from flask_cors import CORS
from PIL import Image
import io
import numpy as np
import tensorflow as tf
import tensorflow.keras.datasets.mnist as mnist
from tensorflow.keras.layers import *

app = Flask(__name__, 
        static_folder="../client/resources/public",
        static_url_path="")
CORS(app)

def init():
    global model, graph, weights
    model = tf.keras.models.Sequential([
        Flatten(input_shape=(28,28)),
        Dense(512, activation=tf.nn.relu),
        Dropout(0.2),
        Dense(10, activation=tf.nn.softmax)
        ])
    optimizer = tf.keras.optimizers.Adam()
    model.compile(optimizer=optimizer,
            loss='sparse_categorical_crossentropy',
            metrics=['accuracy'])
    weights = model.get_weights()
    graph = tf.get_default_graph()

def as_png(x):
    im = Image.fromarray(x * 255.0)
    im = im.convert('RGB').resize((100, 100))
    buf = io.BytesIO() 
    im.save(buf, format='png')
    response = flask.make_response(buf.getvalue())
    response.headers.set('Content-Type', 'image/png')
    return response

def train_data(n):
    X, Y = mnist.load_data()[0]
    X = X / 255.0
    I = np.random.randint(0, X.shape[0], n)
    return X[I], Y[I]

def test_data(m):
    "m is the number of test cases per digit"
    X0, Y0 = mnist.load_data()[-1]
    X0 = X0 / 255.0
    X = []
    for d in range(10):
        X.extend(X0[Y0 == d][:m])
    return np.array(X)

@app.route("/image/<int:digit>/<int:index>")
def GetImage(digit, index):
    X, Y = mnist.load_data()[-1]
    X = X / 255.0
    x = X[Y == digit][index]
    return as_png(x)

@app.route("/learn")
def Learn():
    with graph.as_default():
        model.fit(*train_data(1000), epochs=2)
        P = model.predict(test_data(10)).argmax(axis=1)
        return jsonify(P.tolist())

@app.route("/restart")
def Restart():
    with graph.as_default():
        model.set_weights(weights)
        return "Okay"

@app.route("/")
def Index():
    return app.send_static_file("index.html")

if __name__ == '__main__':
    init()
    app.run(host="0.0.0.0", port=8000, threaded=True)
