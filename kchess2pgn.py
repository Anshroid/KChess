if __name__ != "__main__":
	print("Please run this file standalone rather than as an import.")
	exit(-1)

import argparse
parser = argparse.ArgumentParser(
	prog='KChess2PGN',
	description="Converts a .kchess save file to a .pgn",
	epilog="Made by Anshroid")

import datetime
parser.add_argument("input", help="The input .kchess file", type=argparse.FileType("r"))
parser.add_argument("white", help="The name of the player with the white pieces")
parser.add_argument("black", help="The name of the player with the black pieces")
parser.add_argument("--date", help="The date of the match (in YYYY.MM.DD format)", default=datetime.datetime.now().strftime("%Y.%m.%d"))
parser.add_argument("--term", help="The result of the match (as score-score)", default="")

args = parser.parse_args()

lines = args.input.read().splitlines()

# Discard header (1 -> 10)
# Discard separator (11)

board = [
	[26,22,14,10, 6,14,22,26],
    [ 4, 4, 4, 4, 4, 4, 4, 4],
    [-1,-1,-1,-1,-1,-1,-1,-1],
    [-1,-1,-1,-1,-1,-1,-1,-1],
    [-1,-1,-1,-1,-1,-1,-1,-1],
    [-1,-1,-1,-1,-1,-1,-1,-1],
    [ 2, 2, 2, 2, 2, 2, 2, 2],
    [13,11, 7, 5, 3, 7,11,13]
]

pieces = {
	-1: "-",
    2: "",
    4: "",
    3: "K",
    6: "K",
    5: "Q",
    10: "Q",
    7: "B",
    14: "B",
    11: "N",
    22: "N",
    13: "R",
    26: "R"
}

movetext = ""
move = 1
white = True

def check(directions, piece, board, position, knight=False):
	count = 0
	files = set()
	ranks = set()
	for direction in directions:
		npos = position
		npos = (npos[0] + direction[0], npos[1] + direction[1])
		while npos[0] >= 0 and npos[0] <= 7 and npos[1] >= 0 and npos[1] <= 7:
			if (p := board[npos[0]][npos[1]]) != -1:
				if p == piece:
					count += 1
					files.add(npos[1])
					ranks.add(npos[0])
				break
			if knight:
				break
			npos = (npos[0] + direction[0], npos[1] + direction[1])
	
	if count < 2:
		return False, False

	file = False
	rank = False
	if len(files) < count:
		rank = True
	if len(ranks) < count:
		file = True

	if not file and not rank:
		file = True

	return file, rank


diagonals = [(-1, -1), (-1, 1), (1, 1), (1, -1)]
laterals = [(1, 0), (0, 1), (-1, 0), (0, -1)]


for line in lines[12:]:
	if line in ("---", ""):
		continue

	parts = line.split("m")
	_from = parts[0].split("|")
	_to = parts[1].split("|")
	frompiece = pieces[int(_from[0])]

	# Check if there are multiple possibilities
	addfile = False
	addrank = False

	if frompiece == "":
		if _to[0] != "-1" or "e" in _to[1]:
			if (_to[1][0] == "1" or pieces[board[8 - int(_from[1][1])][int(_to[1][0]) -1 -1]] == "") and \
			   (_to[1][0] == "8" or pieces[board[8 - int(_from[1][1])][int(_to[1][0]) +1 -1]] == ""):
			   addfile = True
	elif frompiece == "Q":
		addfile, addrank = check(diagonals + laterals, int(_from[0]), board, (8 - int(_to[1][1]), int(_to[1][0]) - 1))
	elif frompiece == "R":
		addfile, addrank = check(laterals, int(_from[0]), board, (8 - int(_to[1][1]), int(_to[1][0]) - 1))
	elif frompiece == "B":
		addfile, addrank = check(diagonals, int(_from[0]), board, (8 - int(_to[1][1]), int(_to[1][0]) - 1))
	elif frompiece == "N":
		addfile, addrank = check(
			[(2, 1), (1, 2), (-2, 1), (1, -2), (2, -1), (-1, 2), (-2, -1), (-1, -2)], 
			int(_from[0]), board, (8 - int(_to[1][1]), int(_to[1][0]) - 1), True)

	# Play move	
	board[8 - int(_to[1][1])][int(_to[1][0]) - 1] = int(_from[0])
	board[8 - int(_from[1][1])][int(_from[1][0]) - 1] = -1

	if frompiece == "K" and abs(int(_from[1][0]) - int(_to[1][0])) == 2:
		if _to[1][0] == "3":
			board[7 if white else 0][0] = -1
			board[7 if white else 0][3] = 13 if white else 26
		else:
			board[7 if white else 0][7] = -1
			board[7 if white else 0][5] = 13 if white else 26

	if "e" in _to[1]:
		board[8 - int(_from[1][1])][int(_to[1][0]) - 1] = -1

	if "p" in _to[1]:
		board[8 - int(_to[1][1])][int(_to[1][0]) - 1] = int(_to[1][-1])

	if white:
		movetext += f"{move}. "
		white = False
	else:
		white = True
		move += 1

	
	# Figure out PGN move
	tosquare = f"{chr(int(_to[1][0]) + 96)}{_to[1][1]}"
	
	fromfile = f"{chr(int(_from[1][0]) + 96)}"
	fromrank = _from[1][1]
	frompiece = f"{frompiece}{fromfile if addfile else ''}{fromrank if addrank else ''}"
	
	takes = (fromfile if frompiece == "" else "") + "x" if _to[0] != "-1" else ""
	if frompiece == "K" and abs(int(_from[1][0]) - int(_to[1][0])) == 2:
		movetext += "O-O " if tosquare[0] == "g" else "O-O-O "
		continue

	movetext += frompiece + takes + tosquare + ("=" + pieces[int(_to[1].split("p")[1])] if "p" in _to[1] else "") + " "

print(f"""
[Event "Casual Correspondence game"]
[Site "https://anshroid.github.io/kchess"]
[Date "{args.date}"]
[White "{args.white}"]
[Black "{args.black}"]
{('[Result "' + args.term + '"]') if args.term != "" else ""}

{movetext}
""")