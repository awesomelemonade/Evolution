import zipfile
import os
from PIL import Image

def process(filename='lmcity'):
	zipFilename = filename + '.zip'
	directory = filename
	cfgFilename = filename + '.cfg'

	# Unzip file
	with zipfile.ZipFile(zipFilename, 'r') as file:
		file.extractall('.')

	skyboxMap = {}

	# Loop through all files in subdirectory, convert tga to png - https://stackoverflow.com/questions/61067249/trouble-using-pil-to-change-a-png-to-a-tga
	for directoryFile in os.listdir(directory):
		if directoryFile.endswith(".tga"):
			stripped = directoryFile[:-len(".tga")]
			orientation = stripped[-2:]
			pngFilename = stripped + ".png"

			image = Image.open(directory + "/" + directoryFile)
			image.save(directory + "/" + pngFilename)

			skyboxMap[orientation] = pngFilename
			print("Converted {}={}".format(orientation, directoryFile))


	# Create cfg file
	sizeX = 512
	sizeY = 512
	orientations = ['ft', 'bk', 'up', 'dn', 'rt', 'lf']

	with open(directory + '/' + cfgFilename, 'w') as file:
		file.write(str(sizeX) + " " + str(sizeY) + "\n")
		for orientation in orientations:
			file.write(skyboxMap[orientation] + "\n")
	print("Wrote config file to {}".format(cfgFilename))

filenames = ['mp_deviltooth', 'ame_desert', 'ame_darkgloom', 'ame_siege', 'mp_amh', 'hw_spires', 'mp_crimmind', 'mp_druidcove', 'ame_cotton', 'ame_flatrock', 'ame_bluefreeze', 'ame_iceflats', 'ame_nebula', 'ame_starfield']

for filename in filenames:
	process(filename)

