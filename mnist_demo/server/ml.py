import numpy as np
import tensorflow as tf
mnist = tf.keras.datasets.mnist

class Classifier:
    def __init__(self):
        train, test = mnist.load_data()
        self.Xtrain, self.Ytrain = train
        self.Xtest, self.Ytest = test

        self.Xtrain = self.Xtrain / 255.0
        self.Xtest = self.Xtest / 255.0

        model = tf.keras.models.Sequential([
            tf.keras.layers.Flatten(),
            tf.keras.layers.Dense(512, activation=tf.nn.relu),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(10, activation=tf.nn.softmax),
            ])

        optimizer = tf.keras.optimizers.Adam()
        model.compile(optimizer=optimizer, 
                loss='sparse_categorical_crossentropy',
                metrics=['accuracy'])

        self.model = model
        self.images = self.test_images(20)

    def train(self, n=100):
        N = self.Xtrain.shape[0]
        I = np.random.randint(0, N-1, n)
        X = self.Xtrain[I, :, :]
        Y = self.Ytrain[I]
        self.model.fit(X, Y, epochs=1)

    def predict(self, X):
        return self.model.predict(X)

    def test_digit_images(self, digit, n):
        X = self.Xtest[self.Ytest == digit]
        I = np.random.randint(0, X.shape[0], n)
        return X[I, :, :]

    def test_images(self, n):
        return [self.test_digit_images(i, n) for i in range(10)]

if __name__ == '__main__':
    c = Classifier()
    print("Good: ", c.Xtrain.shape)
    c.train()
    X = c.Xtest[:5,:,:]
    Y = c.Ytest[:5]
    P = c.predict(X)
    print("Prediction:", np.argmax(P, axis=1))
    print("True:      ", Y)
