import { useCallback, useRef, useState } from 'react'
import MappaPost from './MappaPost'
import PostForm from './PostForm'
import { fetchPosts } from './api'

function App() {
  const [posizione, setPosizione] = useState(null)
  const [posts, setPosts] = useState([])

  // Gli ultimi bounds visti servono per ricaricare dopo un salvataggio, quando la
  // mappa non si e' mossa e quindi onIdle non scattera'. In un ref e non in uno stato:
  // cambiano di continuo e non devono provocare un render.
  const ultimiBounds = useRef(null)

  const caricaPost = useCallback(async (bounds) => {
    ultimiBounds.current = bounds
    setPosts(await fetchPosts(bounds))
  }, [])

  return (
    <main>
      <h1>Post sulla mappa</h1>

      <MappaPost
        posizione={posizione}
        onPosizioneChange={setPosizione}
        posts={posts}
        onBoundsChange={caricaPost}
      />

      <PostForm
        posizione={posizione}
        onSalvato={() => {
          setPosizione(null)
          if (ultimiBounds.current) {
            caricaPost(ultimiBounds.current)
          }
        }}
      />
    </main>
  )
}

export default App
