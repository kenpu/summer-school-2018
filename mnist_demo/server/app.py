import flask
from flask import Flask
import ml
from PIL import Image
import io


app = Flask(__name__)
classifier = ml.Classifier()

@app.route("/")
def Index():
    return "Hello:" + str(classifier.Xtrain.shape)

@app.route("/image/<int:digit>/<int:index>")
def TestImage(digit, index):
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

app.run(port=9999, host="0.0.0.0", debug=True)
