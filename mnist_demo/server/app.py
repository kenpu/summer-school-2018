import flask
from flask import Flask, request, jsonify, g, session
import ml
from PIL import Image
import io

app = Flask(__name__)

@app.route("/")
def Index():
    return "Hello"

@app.route("/image/<int:digit>/<int:index>")
def TestImage(digit, index):

    classifier = ml.Classifier()

    n = len(classifier.images[0])
    if not ((0 <= digit <= 9) 
            and (0 <= index < n)):
        return "Not found", 404

    x = classifier.images[digit][index]
    im = Image.fromarray(x * 255)
    im = im.convert('RGB').resize((100, 100))
    buf = io.BytesIO() 
    im.save(buf, format='png')
    response = flask.make_response(buf.getvalue())
    response.headers.set('Content-Type', 'image/png')
    return response

@app.route("/predict")
def Predict():
    global classifier
    # classifier = get_classifier()
    return jsonify(result=classifier.onecycle(100, 1))

