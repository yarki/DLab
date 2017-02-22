
# coding: utf-8

# In[ ]:

import os, cv2, random
import numpy as np
import matplotlib.pyplot as plt
#%matplotlib inline 
from keras.models import Sequential, load_model
from keras.layers import Dropout, Flatten, Convolution2D, MaxPooling2D, Dense, Activation
from keras.optimizers import Adam
from keras.callbacks import Callback, EarlyStopping
from keras.callbacks import BaseLogger, TensorBoard


# In[ ]:

import sys
print sys.executable


# # Constants definition

# In[ ]:

TRAIN_DIR = '/opt/datasets/cats_dogs/train/'
TEST_DIR = '/opt/datasets/cats_dogs/test/'
ROWS = 128
COLS = 128
CHANNELS = 3
TRAIN_IMAGES_COUNT = 2500
PATH_TO_LOGS = '/var/log/tensorboard_py2/'


# # Reading and adjusting images for training

# In[ ]:

all_images = [TRAIN_DIR+i for i in os.listdir(TRAIN_DIR)[:TRAIN_IMAGES_COUNT]]
test_images =  [TEST_DIR+i for i in os.listdir(TEST_DIR)]
random.shuffle(all_images)
test_coeff = int(len (all_images) * .9)

train_images, test_images = all_images[:test_coeff], all_images[test_coeff:]

def read_image(file_path):
    img = cv2.imread(file_path, cv2.IMREAD_COLOR)
    return cv2.resize(img, (ROWS, COLS), interpolation=cv2.INTER_CUBIC).reshape(ROWS, COLS, CHANNELS)

def prepare_data(images):
    count = len(images)
    data = np.ndarray((count, ROWS, COLS, CHANNELS), dtype=np.uint8)

    for i, image_file in enumerate(images):
        image = read_image(image_file)
        data[i] = image#.T
    return data

train = prepare_data(train_images)
test = prepare_data(test_images)


# # Image counts

# In[ ]:

print("Train shape: {}".format(train.shape))
print("Test shape: {}".format(test.shape))


# # Assigning labels to training images

# In[ ]:

labels = []
for i in train_images:
    if 'dog' in i.split("/")[-1] :
        labels.append(1)
    else:
        labels.append(0)
        
labels_test = []
for i in test_images:
    if 'dog' in i.split("/")[-1] :
        labels_test.append(1)
    else:
        labels_test.append(0)


# # Building a convnet

# In[ ]:

optimizer = Adam(lr=1e-6)
objective = 'binary_crossentropy'

def build_model():
    
    model = Sequential()

    model.add(Convolution2D(32, 3, 3, border_mode='same', input_shape=(ROWS, COLS, 3), activation='relu'))
    model.add(Convolution2D(32, 3, 3, border_mode='same', activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))

    model.add(Convolution2D(64, 3, 3, border_mode='same', activation='relu'))
    model.add(Convolution2D(64, 3, 3, border_mode='same', activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    
    model.add(Convolution2D(128, 3, 3, border_mode='same', activation='relu'))
    model.add(Convolution2D(128, 3, 3, border_mode='same', activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))

    model.add(Flatten())
    model.add(Dense(256, activation='relu'))
    model.add(Dropout(0.5))
    
    model.add(Dense(1))
    model.add(Activation('sigmoid'))
    
    model.compile(loss=objective, optimizer=optimizer, metrics=['accuracy'])
    return model


model = build_model()


# # Training the model

# This block takes about 2.5-3 hours to execute if training on whole dataset of 22500 images

# In[ ]:

nb_epoch = 100
batch_size = 16

class LossHistory(Callback):
    def on_train_begin(self, logs={}):
        self.losses = []
        self.val_losses = []
        
    def on_epoch_end(self, batch, logs={}):
        self.losses.append(logs.get('loss'))
        self.val_losses.append(logs.get('val_loss'))

early_stopping = EarlyStopping(monitor='val_loss', patience=5, verbose=1, mode='auto')        
        
def train_and_test_model():
    history = LossHistory()
    tensorboard = TensorBoard(log_dir=PATH_TO_LOGS)
    model.fit(train, labels, batch_size=batch_size, nb_epoch=nb_epoch,
              validation_split=0.25, verbose=2, shuffle=True, callbacks=[history, early_stopping, tensorboard])
    

    predictions = model.predict(test, verbose=2)
    return predictions, history

predictions, history = train_and_test_model()


# # Saving the model and weights

# In[ ]:

path_to_model = '/home/ubuntu/models/model_76.json'
path_to_weights = '/home/ubuntu/models/weigths_76.h5'

model.save(path_to_model)
model.save_weights(path_to_weights)

