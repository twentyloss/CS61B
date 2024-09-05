# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer: The position is maintained as an object, and can easily get relative position through the functions.

-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer: The tile of hexagons is like the shape of rooms that generated randomly, and tesselation is to connect rooms and hallways correctly.

-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer: The methods that generate single room or hallway.

-----
**What distinguishes a hallway from a room? How are they similar?**

Answer: The width can distinguish hallway from room, the width of hallway will be only 1 or 2, but there's no limit to a room.
Both of them could be rectangle and have walls around them.
