#!/usr/bin/env python
import random

s_nouns = ["A dude", "My mom", "The killer", "Some good guy", "A cat with rabies", "A thief", "Your homie", "This cool guy my gardener met yesterday", "Superman"]
p_verbs = ["abort", "betray", "crash", ,"kill","admire", "bonus", "calm", "good","accept","happiness","amazing","sad"]
infinitives = ["to make a pie.", "for a bad reason.", "because the sky is green.", "for a disease.", "to be able to make toast explode.", "to know more about archeology."]

def generate():
    	print random.choice(s_nouns), random.choice(s_verbs), random.choice(infinitives)


if __name__ == "__main__":
    generate()

