
import os
import requests

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(
    title="Vernacular Learning API",
    description="Online Hindi and Santali learning backend",
    version="1.0.0",
)


class TranslationRequest(BaseModel):
    text: str
    source_language: str = "hi"
    target_language: str = "sat"


@app.get("/")
def home():
    return {
        "message": "Vernacular Learning API is running!",
        "mode": "online",
    }


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/translate")
def translate(request: TranslationRequest):

    api_key = os.getenv("BHASINI_API_KEY")

    if not api_key:
        raise HTTPException(
            status_code=500,
            detail="Bhasini API key is not configured.",
        )

    if not request.text.strip():
        raise HTTPException(
            status_code=400,
            detail="Please enter some text.",
        )

    if len(request.text) > 5000:
        raise HTTPException(
            status_code=400,
            detail="Text must be 5000 characters or fewer.",
        )

    try:
        response = requests.post(
            "https://mcp.bhasini.com/v1/translate",
            headers={
                "Authorization": f"Bearer {api_key}",
                "Content-Type": "application/json",
            },
            json={
                "text": request.text,
                "source": request.source_language,
                "target": request.target_language,
            },
            timeout=60,
        )

        response.raise_for_status()
        result = response.json()

        return {
            "original_text": request.text,
            "source_language": request.source_language,
            "target_language": request.target_language,
            "translated_text": result["translation"],
            "status": "success",
        }

    except requests.RequestException as e:
        print("BHASINI ERROR:", str(e))

        if e.response is not None:
            print("BHASINI STATUS:", e.response.status_code)
            print("BHASINI RESPONSE:", e.response.text)

        raise HTTPException(
            status_code=502,
            detail="Translation service error. Check backend terminal."
        )

    except (ValueError, KeyError):
        raise HTTPException(
            status_code=502,
            detail="Unexpected response from translation service."
        )
