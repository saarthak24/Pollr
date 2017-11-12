import re
import tweepy
from tweepy import OAuthHandler
from textblob import TextBlob
from rake_nltk import Rake
import math
import zipcode

commonwords = ['the', 'of', 'and', 'a', 'to', 'in', 'is', 'that', 'it', 'he', 'was',
               'you', 'for', 'on', 'are', 'as', 'with', 'his', 'they', 'at', 'be',
               'this', 'have', 'via', 'from', 'or', 'one', 'had', 'by', 'but', 'not',
               'what', 'all', 'were', 'we', 'RT', 'I', '&', 'when', 'your', 'can', 'said',
               'there', 'use', 'an', 'each', 'which', 'she', 'do', 'how', 'their',
               'if', 'will', 'up', 'about', 'out', 'many', 'then', 'them', 'these', 'so',
               'some', 'her', 'would', 'make', 'him', 'into', 'has', 'two', 'go', 'see', 'no',
               'way', 'could', 'my', 'than', 'been', 'who', 'its', 'did', 'get', 'may',\
               '@', '??', 'Im', 'me', 'u', 'just', 'our', 'like']
 
class TwitterClient(object):
    def __init__(self):
        consumer_key = 'rPFnpIBqkQK9W7jTXaltMxhtI'
        consumer_secret = 'K43xKLT0SFynHJYUYPpnXFWsZx6XQVSU0VrXm0Pos8dyYpayUh'
        access_token = '2380083708-vh9qZ9uMMKNRRQiGadBqowiHo2TvDOHutBcNMJo'
        access_token_secret = 'XrQJe0bhujAZOgwpTzfFMmXhj89hodkiRXJcSFbLjstXh'
 
        try:
            self.auth = OAuthHandler(consumer_key, consumer_secret)
            self.auth.set_access_token(access_token, access_token_secret)
            self.api = tweepy.API(self.auth)

        except:
            print("Error: Authentication Failed")
 
    def clean_tweet(self, tweet):
        return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(\w+:\/\/\S+)", " ", tweet).split())
 
    def get_tweet_sentiment(self, tweet):
        analysis = TextBlob(self.clean_tweet(tweet))
        if analysis.sentiment.polarity > 0:
            return 'positive'
        elif analysis.sentiment.polarity == 0:
            return 'neutral'
        else:
            return 'negative'
 
    def get_tweets(self, query, count = 10):
        tweets = []
 
        try:
            fetched_tweets = self.api.search(q = query, count = count)
 
            for tweet in fetched_tweets:
                parsed_tweet = {}

                parsed_tweet['id'] = tweet.id
                parsed_tweet['text'] = tweet.text
                parsed_tweet['sentiment'] = self.get_tweet_sentiment(tweet.text)
 
                if tweet.retweet_count > 0:
                    if parsed_tweet not in tweets:
                        tweets.append(parsed_tweet)
                else:
                    tweets.append(parsed_tweet)
 
            return tweets
 
        except tweepy.TweepError as e:
            print("Error : " + str(e))

class TwitterClientLoc(object):
    def __init__(self):
        consumer_key = 'rPFnpIBqkQK9W7jTXaltMxhtI'
        consumer_secret = 'K43xKLT0SFynHJYUYPpnXFWsZx6XQVSU0VrXm0Pos8dyYpayUh'
        access_token = '2380083708-vh9qZ9uMMKNRRQiGadBqowiHo2TvDOHutBcNMJo'
        access_token_secret = 'XrQJe0bhujAZOgwpTzfFMmXhj89hodkiRXJcSFbLjstXh'
 
        try:
            self.auth = OAuthHandler(consumer_key, consumer_secret)
            self.auth.set_access_token(access_token, access_token_secret)
            self.api = tweepy.API(self.auth)

        except:
            print("Error: Authentication Failed")
 
    def clean_tweet(self, tweet):
        return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(\w+:\/\/\S+)", " ", tweet).split())
 
    def get_tweet_sentiment(self, tweet):
        analysis = TextBlob(self.clean_tweet(tweet))
        if analysis.sentiment.polarity > 0:
            return 'positive'
        elif analysis.sentiment.polarity == 0:
            return 'neutral'
        else:
            return 'negative'
 
    def get_tweets(self, query, geocode, count = 10):
        tweets = []
 
        try:
            fetched_tweets = self.api.search(q = query, geocode = geocode,  count = count)
 
            for tweet in fetched_tweets:
                parsed_tweet = {}

                parsed_tweet['id'] = tweet.id
                parsed_tweet['text'] = tweet.text
                parsed_tweet['sentiment'] = self.get_tweet_sentiment(tweet.text)
 
                if tweet.retweet_count > 0:
                    if parsed_tweet not in tweets:
                        tweets.append(parsed_tweet)
                else:
                    tweets.append(parsed_tweet)
 
            return tweets
 
        except tweepy.TweepError as e:
            print("Error : " + str(e))
 
def main(term, geocode, num, count):
    json = {}
    json['word'] = term
    api = TwitterClient()
    tweets = api.get_tweets(query = term, count = count)
    
    json['national'] = {}
    ptweets = [tweet for tweet in tweets if tweet['sentiment'] == 'positive']
    json['national']['pos'] = 100*len(ptweets)/len(tweets)
    ntweets = [tweet for tweet in tweets if tweet['sentiment'] == 'negative']
    json['national']['neg'] = 100*len(ntweets)/len(tweets)

    posids = []
    negids = []
    for tweet in ptweets[:num]:
        posids.append(tweet['id'])
    for tweet in ntweets[:num]:
        negids.append(tweet['id'])
    json['national']['pos_ids'] = posids
    json['national']['neg_ids'] = negids

    apiLoc = TwitterClientLoc()
    tweets = apiLoc.get_tweets(query = term , geocode = geocode, count = count)

    json['local'] = {}
    ptweets = [tweet for tweet in tweets if tweet['sentiment'] == 'positive']
    json['local']['pos'] = 100*len(ptweets)/len(tweets)
    ntweets = [tweet for tweet in tweets if tweet['sentiment'] == 'negative']
    json['local']['neg'] = 100*len(ntweets)/len(tweets)

    
    posids = []
    negids = []
    for tweet in ptweets[:num]:
        posids.append(tweet['id'])
    for tweet in ntweets[:num]:
        negids.append(tweet['id'])
    json['local']['pos_ids'] = posids
    json['local']['neg_ids'] = negids
    return json

def words(text):
    r = Rake()
    r.extract_keywords_from_text(text)

    results = r.get_ranked_phrases()
    results = results[:int(math.log2(len(results)))]
    final = [];
    for word in results:
        if word in commonwords:
            continue
        for asdf in commonwords:
            if asdf in word.strip():
                continue
        final.append(word)
    return final
 
if __name__ == "__main__":
    myzip = zipcode.isequal('44102')
    geocode = str(myzip.lat) + "," + str(myzip.lon) + ",150mi"
    text = """Stephen Curry once again used his platform to make his voice heard on Saturday, penning an entry in The Players' Tribune on Colin Kaepernick, Veterans Day, respect for the military, and the time President Trump tweeted that he had rescinded his White House invitation to Curry and the NBA champion Golden State Warriors shortly after Curry and teammates reiterated their stance that they didn't want to go."""
    json = {}
    data = []
    for word in words(text):
        try:
            data.append(main(word, geocode, 1, 500))
        except ZeroDivisionError as e:
            continue
    json['num_words'] = len(data)
    json['words'] = data
    print(json)