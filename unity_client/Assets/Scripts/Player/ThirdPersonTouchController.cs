using UnityEngine;

namespace CRMP.Player
{
    [RequireComponent(typeof(CharacterController))]
    public class ThirdPersonTouchController : MonoBehaviour
    {
        [Header("Movement")]
        public float walkSpeed = 3.8f;
        public float sprintSpeed = 6.2f;
        public float gravity = -19.62f;

        [Header("Mobile Input (Virtual Joystick)")]
        public Vector2 moveInput;
        public bool isSprinting;

        private CharacterController _controller;
        private Vector3 _velocity;

        private void Awake()
        {
            _controller = GetComponent<CharacterController>();
        }

        private void Update()
        {
            Vector3 move = new Vector3(moveInput.x, 0f, moveInput.y);
            if (move.sqrMagnitude > 0.01f)
            {
                transform.forward = Vector3.Slerp(transform.forward, move.normalized, Time.deltaTime * 10f);
            }

            float speed = isSprinting ? sprintSpeed : walkSpeed;
            _controller.Move(move.normalized * (speed * Time.deltaTime));

            if (_controller.isGrounded && _velocity.y < 0f)
                _velocity.y = -2f;

            _velocity.y += gravity * Time.deltaTime;
            _controller.Move(_velocity * Time.deltaTime);
        }
    }
}
