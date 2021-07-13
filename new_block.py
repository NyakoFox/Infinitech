# written in 3 seconds cause i hate making 500 files just to make a block don't judge my code

MODID = "infinitech"

def color(code):
    return f"\033[0;{code}m"

def main():
    block_name = input(f"{color('37;1;4')}Enter the name of the block:\n{color('31;1')}> {color('0')}")
    print(f"{color('37;1;4;6')}Nice block")
    
    
    with open(f"src/main/resources/assets/{MODID}/blockstates/{block_name}.json", "x") as file:
        file.write(
"""{
  \"variants\": {
    \"\": { "model": \"""" + MODID +""":block/""" + block_name + """\" }
  }
}""")

    print(f"{color('0')}src/main/resources/assets/{MODID}/blockstates/{block_name}.json")

    with open(f"src/main/resources/assets/{MODID}/models/block/{block_name}.json", "x") as file:
        file.write(
"""{
  \"parent\": \"block/cube_all\",
  \"textures\": {
    \"\": { "all": \"""" + MODID +""":block/""" + block_name + """\" }
  }
}""")

    print(f"src/main/resources/assets/{MODID}/models/block/{block_name}.json")

    with open(f"src/main/resources/assets/{MODID}/models/item/{block_name}.json", "x") as file:
        file.write(
"""{
    "parent": \"""" + MODID +""":block/""" + block_name + """\"
  }
}""")

    print(f"src/main/resources/assets/{MODID}/models/item/{block_name}.json")

    with open(f"src/main/resources/data/{MODID}/loot_tables/blocks/{block_name}.json", "x") as file:
        file.write(
"""{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        {
          "type": "minecraft:item",
          "name": \"""" + MODID + """:""" + block_name + """"
        }
      ],
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ]
    }
  ]
}""")

    print(f"src/main/resources/data/{MODID}/loot_tables/blocks/{block_name}.json")

    print(f"{color('0')}created the hecking ,,,, stuff! it worked,,")
    

if __name__ == "__main__":
    main()
