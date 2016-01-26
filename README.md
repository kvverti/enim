# ENIM Custom Entity Model Format
As you may know, entity models were hinted for 1.9, then (most likely) scrapped.
So, I have taken it upon myself to create a working customizable entity model system as a mod.
Here's the progress so far:

###Entity JSON Files
These JSON model files will contain model element definitions and animation declarations.
A typical model file might look like this.
```JSON
{
    "imports": {
        "enim:entityImport": ["base_from_parent"]
    },
    "elements": [
        {
          "name": "base",
          "texcoords": [0,8],
          "from": [4,0,4],
          "to": [12,4,12],
          "rotpoint": [0,0,0],
          "parent": ""
        },
        {
          "name": "node",
          "texcoords": [0,0],
          "from": [6,2,6],
          "to": [10,6,10],
          "rotpoint": [0,0,0],
          "parent": "base"
        }
    ],
    "animations": {
        "idle": {
            "script": "enim:entityScript",
            "with": {
                "node": "node"
            }
        }
    }
}
```
Here's the breakdown:
* **Imports:** These are elements imported from other files.
Each key is a resource location pointing to a `.json` file relative to the `/models/entity` directory.
The one in the example points to `assets/enim/models/entity/entityImport.json`.
Each value in the array is the name of the element to import.
A special value `"*"`can be used to import all the elements in a particular file.
You can only import elements defined within the import file; imports will not be recursively imported.
* **Elements:** This is similar to the elements used in block models, but with slightly different syntax.
  * `name`: The element name is mandatory and must be unique.
  * `parent`: (optional) In the code, elements can be grouped on the same rendering grid using a parent-child relation.
  Child element will share the same rotations as the parent element, although they can be rotated individualy
  relative to the parent.
  The value of this tag is the name of the parent element, or `""`, `null`, or absent if there is no parent.
  * `texcoords`: The coordinates on the texture image.
  These are the upper-left corner of the rectangular region containing the texture.
  If the dimensions are `[x, y, z]`, the region will have a height of `z + y` and a width of `2z + 2x`.
  * `from`: The lowest vertex of the element. Analogous to `from` in block models.
  * `to`: The highest vertex of the element. Analogous to `to` in block models.
  * `rotpoint`: (optional) This is the origin of rotation for the element, relative to the block region.
* **Animations:** These are not texture animations! This is the place to animate the model under certain conditions, as follows.
  * `idle`: When the entity is not controlled by AI.
  * `moving`: When the entity is moving. The speed of this animation is modulated by the speed of the entity.
  * `attack`: When the entity is attacking.
  * `defend` (?): When the entity is being attacked.
  * `damaged`: When the entity is damaged.
  * `swimming` (?): When the entity is swimming.
  * `flying` (?): When the entity is flying.
  * `interact`: When a player interacts with the entity by right-clicking.
  This animation is played in reverse when the player finishes interaction (?).
  * Within each of these are these properties.
    * `script`: The animation script to use. This is a resource location pointing to a `.enim` file relative to `/models/entity`.
    See ENIM files below.
    * `with`: This connects the animation-defined elements with actual elements of the entity.
    The key is the animation-defined element, the value is the model-defined element.

###Entity Animation (ENIM) Files
These are where the entity animations are defined. A typical animation file might look like this.
```
#this is a comment
#they reach until the end of the line

#definition
define node

#frequency or "tick delay". freq 2 occurs every two ticks, freq 3 every three, etc.
freq 1

set node y 0.0
set node y 30.0
set node y 60.0
set node y 90.0
set node y 120.0
set node y 150.0
set node y 180.0
 
repeat 6 loop
rotate node y 30.0
end loop

#empty frames
pause 387
```
These are the commands so far.
* `define <name>`: This defines an element placeholder to be used in the animation.
The actual elements are defined in the model file.
* `freq <unsigned_int>`: This is similar to frametime in block animations.
* `set <name> <axis> <floating_point>`: Sets the angle of a particular element along one axis.
* `rotate <name> <axis> <floating_point>`: Adds the specified angle to the element along one axis.
* `pause <unsigned_int>`: Does nothing for the specified number of frames.
* `repeat <unsigned_int> <name>...end <name>`: Defines a loop execute the given number of times.
This is most useful for rotate commands.
* `#<text>`: This is a comment. Comments continue until the end of the line.

This is still in early developement and not all features are currently implemented.
